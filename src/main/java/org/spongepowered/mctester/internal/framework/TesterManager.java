package org.spongepowered.mctester.internal.framework;

import net.minecraft.client.Minecraft;
import org.junit.Assert;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackComparators;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.mctester.internal.McTesterDummy;
import org.spongepowered.mctester.internal.appclass.ErrorSlot;
import org.spongepowered.mctester.internal.event.ErrorPropagatingEventListener;
import org.spongepowered.mctester.internal.event.OneShotEventListener;
import org.spongepowered.mctester.junit.TestUtils;
import org.spongepowered.mctester.internal.event.StandaloneEventListener;
import org.spongepowered.mctester.internal.framework.proxy.RemoteClientProxy;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.mctester.internal.framework.proxy.ProxyCallback;
import org.spongepowered.mctester.internal.interfaces.IMixinMinecraft;
import org.spongepowered.mctester.junit.Client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class TesterManager implements /*Runnable,*/ TestUtils, ProxyCallback {

    //public Game fakeGame;
    public ServerSideClientHandler client;
    private Set<ErrorSlot> errorSlots = new HashSet<>();

    /*public static void runTestThread() {
        TesterManager manager = new TesterManager();
        Thread thread = new Thread(manager);
        thread.setName("Tester-thread");
        thread.start();
    }*/

    public TesterManager() {
        //Game realGame = Sponge.getGame();
        //this.fakeGame = MainThreadProxy.newProxy(realGame, Game.class, null);
        this.client = new ServerSideClientHandler(RemoteClientProxy.newProxy(this));
    }

    /*@Override
    public void run() {


        //ChatTest chatTest = new ChatTest();
        //chatTest.runTest(fakeGame, client, this);
    }*/

    @Override
    public Game getGame() {
        return Sponge.getGame();
    }

    @Override
    public Client getClient() {
        return this.client;
    }

    @Override
    public Player getThePlayer() {
        Collection<Player> players = Sponge.getServer().getOnlinePlayers();
        if (players.size() != 1) {
            throw new RuntimeException("Unexpected players: " + players);
        }
        return players.iterator().next();
    }

    @Override
    public void listenOneShot(Runnable runnable, StandaloneEventListener<? extends Event>... listeners) throws Throwable {
        /*AssertionError error = new AssertionError("The one shot event listener registered here failed to run in time!\n");
        error.fillInStackTrace();*/

        if (listeners == null || listeners.length == 0) {
            throw new IllegalArgumentException("Must provide at least one listener!");
        }

        List<OneShotEventListener<?>> newListeners = this.setupOneShotListeners(listeners);
        runnable.run();
        this.finishOneShotListeners(newListeners);



        /*OneShotEventListener<T> oneShot = new OneShotEventListener<>(eventClass, listener, this.errorSlot, error);
        Sponge.getEventManager().registerListener(McTesterDummy.INSTANCE, eventClass, oneShot);
        this.listeners.add(oneShot);*/

        //return oneShot;
    }

    private List<OneShotEventListener<?>> setupOneShotListeners(StandaloneEventListener<? extends Event>... listeners) {
        List<OneShotEventListener<?>> newListeners = new ArrayList<>(listeners.length);
        for (EventListener<?> listener: listeners) {
            OneShotEventListener<?> newListener = new OneShotEventListener((StandaloneEventListener) listener, this.makeErrorSlot());
            Sponge.getEventManager().registerListener(McTesterDummy.INSTANCE, newListener.getEventClass(), (EventListener) newListener);
            newListeners.add(newListener);
        }

        return newListeners;
    }

    public void beforeTest() {
        this.errorSlots.clear();

        // Wait for a packet round-trip to ensure that the client has processed previous packets,
        // like chunk data. This can help avoid race conditions later, by ensuring that client
        // ticks will be fast when a test starts executing.
        this.client.onFullyLoggedIn();
        ((IMixinMinecraft) Minecraft.getMinecraft()).setAllowPause(false);
    }

    private void finishOneShotListeners(List<OneShotEventListener<?>> listeners) throws Throwable {
        // Throw any caught exceptions before checking if listeners ran.
        for (OneShotEventListener<?> listener: listeners) {
            listener.throwCaughtException();
        }

        for (OneShotEventListener listener: listeners) {
            if (!listener.handleFinished.isDone()) {
                throw new AssertionError("A one-shot listener failed to run in time!\n" +
                        "By the time this method call finished, a one-shot event listener should have been run - but it wasn't.\n");
            }
            Sponge.getEventManager().unregisterListeners(listener);
        }
    }

    @Override
    public <T extends Event> StandaloneEventListener<T> listen(StandaloneEventListener<T> listener) {
        ErrorPropagatingEventListener<T> newListener = new ErrorPropagatingEventListener<>(listener, this.makeErrorSlot());
        Sponge.getEventManager().registerListener(McTesterDummy.INSTANCE, newListener.getEventClass(), newListener);
        return newListener;
    }

    public ErrorSlot makeErrorSlot() {
        ErrorSlot slot = new ErrorSlot();
        this.errorSlots.add(slot);
        return slot;
    }


    private void checkErrorSlots() throws Throwable {
        for (ErrorSlot slot: this.errorSlots) {
            slot.throwIfSet();
        }
    }


    public void checkAndClearErrorSlots() throws Throwable {
        this.checkErrorSlots();
    }

    @Override
    public <T extends Event> int listenTimeout(Runnable runnable, StandaloneEventListener<T> listener, int ticks) throws Throwable {
        /*AssertionError error = this.makeFakeException(String.format("The one shot event listener registered here failed to run in %s ticks!\n", ticks));*/

        OneShotEventListener<T> oneShot = new OneShotEventListener<>(listener, this.makeErrorSlot());
        Sponge.getEventManager().registerListener(McTesterDummy.INSTANCE, oneShot.getEventClass(), oneShot);

        // We use handleStarted, so that a long-running event listener doesn't trip the timeout.
        // At the end of this method, we wait for handleFinished to ensure
        // that we re-throw any exception that may have occured.
        CompletableFuture<Long> handledFuture = oneShot.handleStarted;
        CompletableFuture<Void> timeoutFuture = new CompletableFuture<>();

        long startTime = System.currentTimeMillis();
        Task task = Sponge.getScheduler().createTaskBuilder().delayTicks(ticks).execute(() -> timeoutFuture.complete(null)).submit(McTesterDummy.INSTANCE);

        runnable.run();

        // Blocking
        Object result;
        try {
            result = CompletableFuture.anyOf(handledFuture, timeoutFuture).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        task.cancel();

        // If we don't get a Long result, we hit our timeout, which means that the maximum amount of time elapsed
        int elapsedTicks = ticks;
        if (result instanceof Long) {
            elapsedTicks = (int) ((Long) result - startTime) / 50;
        }

        if (!handledFuture.isDone()) {
            throw new AssertionError("A timed listener failed to run in the alloted number of ticks!\n" +
                                     "A timed listener had " + ticks + " ticks to fire, but it didn't.\n" +
                                     "See the nested stacktrace for the location of the timed event handler.", oneShot.fakeError);
        }

        oneShot.handleFinished.get();
        this.checkErrorSlots();

        return Math.max(ticks - elapsedTicks, 0);
    }

    private AssertionError makeFakeException(String message) {
        AssertionError assertionError = new AssertionError(message);
        assertionError.fillInStackTrace();

        // We remove the first two StackTraceElements, which correspond to the invocation
        // of this method and its caller. This causes the first element to point
        // to the user's code (e.g. an invocation of listenTimeout)
        int shortLength = assertionError.getStackTrace().length - 2;
        StackTraceElement[] modified = new StackTraceElement[shortLength];
        System.arraycopy(assertionError.getStackTrace(), 2, modified, 0, shortLength);

        assertionError.setStackTrace(modified);

        return assertionError;
    }

    @Override
    public <T> T runOnMainThread(Callable<T> callable) throws Throwable {
        if (Sponge.getServer().isMainThread()) {
            throw new IllegalStateException("Calling runOnMainThread from the main thread is pointless!");
        }

        try {
            return McTesterDummy.INSTANCE.syncExecutor.schedule(callable, 0, TimeUnit.SECONDS).get();
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Override
    public void runOnMainThread(Runnable runnable) throws Throwable {
        if (Sponge.getServer().isMainThread()) {
            throw new IllegalStateException("Calling runOnMainThread from the main thread is pointless!");
        }

        this.runOnMainThread(() -> {
            runnable.run();
            return null;
        });
    }

    @Override
    public void sleepTicks(int ticks) {
        if (Sponge.getServer().isMainThread()) {
            throw new IllegalStateException("Can't call TestUtils.sleepTicks from the main thread!");
        }
        FutureTask<?> task = new FutureTask<>((() -> {}), null);
        Sponge.getScheduler().createTaskBuilder().delayTicks(ticks).execute(task).submit(McTesterDummy.INSTANCE);
        try {
            task.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterInvoke() throws Throwable {

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

    @Override
    public void waitForInventoryPropagation() {
        this.sleepTicks(2);
    }

    @Override
    public void waitForEntitySpawn() {
        this.sleepTicks(2);
    }

    @Override
    public void waitForAll() {
        this.sleepTicks(2);
    }

    @Override
    public void assertStacksEqual(ItemStack serverStack, ItemStack clientStack) {
        Assert.assertEquals("Itemstacks are not equal! Server: " + serverStack + " client " + clientStack, 0, ItemStackComparators.ALL.compare(serverStack, clientStack));
    }
}
