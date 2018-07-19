package org.spongepowered.mctester.internal.framework

import net.minecraft.client.Minecraft
import net.minecraft.server.MinecraftServer
import net.minecraft.server.management.PlayerChunkMapEntry
import net.minecraft.world.WorldServer
import net.minecraft.world.chunk.Chunk
import org.junit.Assert
import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Event
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.item.inventory.ItemStackComparators
import org.spongepowered.mctester.internal.McTester
import org.spongepowered.mctester.internal.McTesterDummy
import org.spongepowered.mctester.internal.appclass.ErrorSlot
import org.spongepowered.mctester.internal.coroutine.await
import org.spongepowered.mctester.internal.event.ErrorPropagatingEventListener
import org.spongepowered.mctester.internal.event.OneShotEventListener
import org.spongepowered.mctester.internal.event.StandaloneEventListener
import org.spongepowered.mctester.internal.framework.proxy.ProxyCallback
import org.spongepowered.mctester.internal.framework.proxy.RemoteClientProxy
import org.spongepowered.mctester.internal.interfaces.IMixinMinecraft
import org.spongepowered.mctester.internal.interfaces.IMixinPlayerChunkMap
import org.spongepowered.mctester.junit.TestUtils
import java.util.*
import java.util.concurrent.*
import java.util.function.Consumer

class TesterManager :/*Runnable,*/ TestUtils, ProxyCallback {

    //public Game fakeGame;
    override var client: ServerSideClientHandler = ServerSideClientHandler(RemoteClientProxy.newProxy(this))
    private val errorSlots = HashSet<ErrorSlot>()

    /*@Override
    public void run() {


        //ChatTest chatTest = new ChatTest();
        //chatTest.runTest(fakeGame, client, this);
    }*/

    override val thePlayer: Player
        get() {
            val players = Sponge.getServer().onlinePlayers
            if (players.size != 1) {
                throw RuntimeException("Unexpected players: $players")
            }
            return players.iterator().next()
        }
    /*public static void runTestThread() {
        TesterManager manager = new TesterManager();
        Thread thread = new Thread(manager);
        thread.setName("Tester-thread");
        thread.start();
    }*/

    @Throws(Throwable::class)
    override fun <T: Event> listenOneShot(runnable: Runnable, vararg listeners: StandaloneEventListener<T>) {
        /*AssertionError error = new AssertionError("The one shot event listener registered here failed to run in time!\n");
        error.fillInStackTrace();*/

        if (listeners == null || listeners.size == 0) {
            throw IllegalArgumentException("Must provide at least one listener!")
        }

        val newListeners = this.setupOneShotListeners(*listeners)
        runnable.run()
        this.finishOneShotListeners(newListeners)


        /*OneShotEventListener<T> oneShot = new OneShotEventListener<>(eventClass, listener, this.errorSlot, error);
        Sponge.getEventManager().registerListener(McTesterDummy.INSTANCE, eventClass, oneShot);
        this.listeners.add(oneShot);*/

        //return oneShot;
    }

    override suspend fun <T : Event> listenOneShotSuspend(block: suspend () -> Unit, vararg listeners: StandaloneEventListener<T>) {
        if (listeners.size == 0) {
            throw IllegalArgumentException("Must provide at least one listener!")
        }

        val newListeners = this.setupOneShotListeners(*listeners)
        block()
        this.finishOneShotListeners(newListeners)
    }

    private fun <T: Event> setupOneShotListeners(vararg listeners: StandaloneEventListener<T>): List<OneShotEventListener<out T>> {
        val newListeners = ArrayList<OneShotEventListener<out T>>(listeners.size)
        for (listener in listeners) {
            val newListener = OneShotEventListener(listener, this.makeErrorSlot())
            Sponge.getEventManager().registerListener<T>(McTesterDummy.INSTANCE, newListener.eventClass, newListener)
            newListeners.add(newListener)
        }

        return newListeners
    }


    fun beforeTest() {
        this.errorSlots.clear()
        this.waitForFullLogin()
        (Minecraft.getMinecraft() as IMixinMinecraft).setAllowPause(false)
    }

    // TODO: REVIST THIS IF WE EVER ADD MULTI-CLIENT SUPPORT!!!
    // The chunk logic will need to change
    private fun waitForFullLogin() {
        // At this point, the player has already joined the world. This means that all of
        // the necessary PlayerChunkMapEntry's have been created via PlayerChunkMap#addPlayer
        // (this is called during initializeConnectionToPlayer, before ClientConnectionEvent.Join is fired)
        // However, the chunk data is sent during the subsequent tick.
        // We need to ensure that the client has received the chunk data packets before we start
        // running tests. Otherwise, blocks may not yet exist on the client, which will lead to issues
        // when a test instructs a client to click a block.

        // To ensure that all of the necessary packet have been sent, we want until PlayerChunkMap
        // has pendingSendToPlayers, entriesWithoutChunks, and dirtyEntries empty.
        // Since all of the chunks in the player's view radius are added to this map during
        // PlayerChunkMap#addPlayer, we know that all of the player's chunks have been sent when this is empty.
        // Note that new entires could be added to dirtyEntires while we wait, delaying our tests further.
        // However, all of these lists will eventually be empty. Waiting for them all to be empty
        // guarantees that we've waited at least as long as is necessary for the client to have recxieved
        // all of its chunk packets for its iniital spawn position.

        val chunkMapEntryFuture = CompletableFuture<Void>()
        Sponge.getScheduler().createTaskBuilder()
                .intervalTicks(1)
                .name("McTester chunk checker")
                .execute { task ->
                    val chunkMap = ((McTester.getThePlayer().world as WorldServer).playerChunkMap) as IMixinPlayerChunkMap

                    // Some chunks amy be loaded without some of their neighbor chunks loaded
                    // These chunks will set in PlayerChunkEntyMap, but won't be populated or
                    // sent until their neighbors are loaded.
                    // These chunks prevent pendingSendToPlayers and entriesWithoutChunks from ever
                    // being completely empty, so we check for chuks that have isTerrainPopulated or isLightPopulated
                    // set to false
                    val chunkEmptyOrUnpopulated = { entry: PlayerChunkMapEntry ->
                        val chunk = entry.chunk
                        chunk == null || (!chunk.isTerrainPopulated || !chunk.isLightPopulated)
                    }

                    val noDirty = chunkMap.dirtyEntries.isEmpty()
                    val noEntriesWithoutChunks = chunkMap.entriesWithoutChunks.all(chunkEmptyOrUnpopulated)
                    val noPendigSendToPlayesr = chunkMap.pendingSendToPlayers.all(chunkEmptyOrUnpopulated)

                    if (noDirty && noEntriesWithoutChunks && noPendigSendToPlayesr) {
                        chunkMapEntryFuture.complete(null)
                        task.cancel()
                    }
                }
                .submit(McTester.INSTANCE)

        chunkMapEntryFuture.get()

        // Wait for a packet round-trip to ensure that the client has processed previous packets,
        // like chunk data. This can help avoid race conditions later, by ensuring that client
        // ticks will be fast when a test starts executing.
        this.client.onFullyLoggedIn()

    }

    @Throws(Throwable::class)
    private fun finishOneShotListeners(listeners: List<OneShotEventListener<*>>) {
        // Throw any caught exceptions before checking if listeners ran.
        for (listener in listeners) {
            listener.throwCaughtException()
        }

        for (listener in listeners) {
            if (!listener.handleFinished.isDone) {
                throw AssertionError("A one-shot listener failed to run in time!\n" + "By the time this method call finished, a one-shot event listener should have been run - but it wasn't.\n")
            }
            Sponge.getEventManager().unregisterListeners(listener)
        }
    }

    override fun <T : Event> listen(listener: StandaloneEventListener<T>): StandaloneEventListener<T> {
        val newListener = ErrorPropagatingEventListener(listener, this.makeErrorSlot())
        Sponge.getEventManager().registerListener(McTesterDummy.INSTANCE, newListener.eventClass, newListener)
        return newListener
    }

    fun makeErrorSlot(): ErrorSlot {
        val slot = ErrorSlot()
        this.errorSlots.add(slot)
        return slot
    }


    @Throws(Throwable::class)
    private fun checkErrorSlots() {
        for (slot in this.errorSlots) {
            slot.throwIfSet()
        }
    }


    @Throws(Throwable::class)
    fun checkAndClearErrorSlots() {
        this.checkErrorSlots()
    }

    @Throws(Throwable::class)
    override fun <T : Event> listenTimeout(runnable: Runnable, listener: StandaloneEventListener<T>, ticks: Int): Int {
        /*AssertionError error = this.makeFakeException(String.format("The one shot event listener registered here failed to run in %s ticks!\n", ticks));*/

        val oneShot = OneShotEventListener(listener, this.makeErrorSlot())
        Sponge.getEventManager().registerListener(McTesterDummy.INSTANCE, oneShot.eventClass, oneShot)

        // We use handleStarted, so that a long-running event listener doesn't trip the timeout.
        // At the end of this method, we wait for handleFinished to ensure
        // that we re-throw any exception that may have occured.
        val handledFuture = oneShot.handleStarted
        val timeoutFuture = CompletableFuture<Void>()

        val startTime = System.currentTimeMillis()
        val task = Sponge.getScheduler().createTaskBuilder().delayTicks(ticks.toLong()).execute(Runnable {timeoutFuture.complete(null)}).submit(McTesterDummy.INSTANCE)

        runnable.run()

        // Blocking
        val result: Any?
        try {
            result = CompletableFuture.anyOf(handledFuture, timeoutFuture).get()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

        task.cancel()

        // If we don't get a Long result, we hit our timeout, which means that the maximum amount of time elapsed
        var elapsedTicks = ticks
        if (result is Long) {
            elapsedTicks = (result - startTime).toInt() / 50
        }

        if (!handledFuture.isDone) {
            throw AssertionError("A timed listener failed to run in the alloted number of ticks!\n" +
                    "A timed listener had " + ticks + " ticks to fire, but it didn't.\n" +
                    "See the nested stacktrace for the location of the timed event handler.", oneShot.fakeError)
        }

        oneShot.handleFinished.get()
        this.checkErrorSlots()

        return Math.max(ticks - elapsedTicks, 0)
    }

    // TODO - Reduce the duplication between these two methods in a way that's still readable.
    // We can't all 'listenTimeout' from 'listenTimeOutSuspend', since 'listenTimeout' can't call await()
    // or call the suspend block
    // We also can't call 'listenTimeoutSuspend' from 'listenTimeout', since 'listenTimeout' isn't suspendable
    override suspend fun <T : Event> listenTimeOutSuspend(block: suspend () -> Unit, listener: StandaloneEventListener<T>, ticks: Int): Int {
        val oneShot = OneShotEventListener(listener, this.makeErrorSlot())
        Sponge.getEventManager().registerListener(McTesterDummy.INSTANCE, oneShot.eventClass, oneShot)

        // We use handleStarted, so that a long-running event listener doesn't trip the timeout.
        // At the end of this method, we wait for handleFinished to ensure
        // that we re-throw any exception that may have occured.
        val handledFuture = oneShot.handleStarted
        val timeoutFuture = CompletableFuture<Void>()

        val startTime = System.currentTimeMillis()
        val task = Sponge.getScheduler().createTaskBuilder().delayTicks(ticks.toLong()).execute(Runnable {timeoutFuture.complete(null)}).submit(McTesterDummy.INSTANCE)

        block()

        val result = CompletableFuture.anyOf(handledFuture, timeoutFuture).await()

        task.cancel()

        // If we don't get a Long result, we hit our timeout, which means that the maximum amount of time elapsed
        var elapsedTicks = ticks
        if (result is Long) {
            elapsedTicks = (result - startTime).toInt() / 50
        }

        if (!handledFuture.isDone) {
            throw AssertionError("A timed listener failed to run in the allotted number of ticks!\n" +
                    "A timed listener had " + ticks + " ticks to fire, but it didn't.\n" +
                    "See the nested stacktrace for the location of the timed event handler.", oneShot.fakeError)
        }

        oneShot.handleFinished.await()
        this.checkErrorSlots()

        return Math.max(ticks - elapsedTicks, 0)
    }

    private fun makeFakeException(message: String): AssertionError {
        val assertionError = AssertionError(message)
        assertionError.fillInStackTrace()

        // We remove the first two StackTraceElements, which correspond to the invocation
        // of this method and its caller. This causes the first element to point
        // to the user's code (e.g. an invocation of listenTimeout)
        val shortLength = assertionError.stackTrace.size - 2
        val modified = arrayOfNulls<StackTraceElement>(shortLength)
        System.arraycopy(assertionError.stackTrace, 2, modified, 0, shortLength)

        assertionError.stackTrace = modified

        return assertionError
    }

    @Throws(Throwable::class)
    override fun <T> runOnMainThread(callable: Callable<T>): T {
        if (Sponge.getServer().isMainThread) {
            throw IllegalStateException("Calling runOnMainThread from the main thread is pointless!")
        }

        try {
            return McTesterDummy.INSTANCE.syncExecutor.schedule(callable, 0, TimeUnit.SECONDS).get()
        } catch (e: ExecutionException) {
            throw e.cause!!
        }

    }

    @Throws(Throwable::class)
    override fun runOnMainThread(runnable: Runnable) {
        if (Sponge.getServer().isMainThread) {
            throw IllegalStateException("Calling runOnMainThread from the main thread is pointless!")
        }

        this.runOnMainThread(Callable<Void> {
            runnable.run()
            null
        })
    }

    override fun sleepTicks(ticks: Int) {
        if (Sponge.getServer().isMainThread) {
            throw IllegalStateException("Can't call TestUtils.sleepTicks from the main thread!")
        }
        val task = FutureTask<Any>({ }, null)
        Sponge.getScheduler().createTaskBuilder().delayTicks(ticks.toLong()).execute(task).submit(McTesterDummy.INSTANCE)
        try {
            task.get()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

    @Throws(Throwable::class)
    override fun afterInvoke() {

        // Run this first, since it was
        /*this.checkErrorSlots();


        // This runs after a Client method has returned
        for (OneShotEventListener listener: this.listeners) {
            if (!listener.handleFinished.isDone()) {
                throw new AssertionError("A one-shot listener failed to run in time!\n" +
                                         "By the time this method call finished, a one-shot event listener should have been run - but it wasn't.\n"  +
                                         "See the nested stacktrace for the location of the one-shot event handler.", listener.fakeError);
            }
            Sponge.getEventManager().unregisterListeners(listener);
        }
        listeners.clear();*/
    }

    override fun waitForInventoryPropagation() {
        this.sleepTicks(2)
    }

    override fun waitForEntitySpawn() {
        this.sleepTicks(2)
    }

    override fun waitForAll() {
        this.sleepTicks(2)
    }

    override fun assertStacksEqual(serverStack: ItemStack, clientStack: ItemStack) {
        Assert.assertEquals("Itemstacks are not equal! Server: $serverStack client $clientStack", 0, ItemStackComparators.ALL.compare(serverStack, clientStack).toLong())
    }

}
