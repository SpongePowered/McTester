/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.mctester.junit

import org.junit.Assert
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Event
import org.spongepowered.api.event.EventManager
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.mctester.internal.event.StandaloneEventListener
import java.util.concurrent.Callable

/**
 * Helper utilities useful in writing tests against Minecraft
 */
interface TestUtils {

    /**
     * Returns the main interface for controlling the client.
     *
     *
     * Unlike normal SpongeAPI methods, all methods in this class operate directly
     * on the client thread. They mimic, as closely as possible, the actions
     * of a human interacting with the Minecraft client.
     *
     *
     * To control the client using the normal SpongeAPI methods, use [.getThePlayer]
     *
     * @return
     */
    val client: Client

    /**
     * Gets the player connected to the server.
     *
     *
     * This is a normal SpongeAPI [Player]. This
     * method is a shortcut to get the one player connected to
     * the server, since only one player is connected in the test
     * environment.
     * @return
     */
    val thePlayer: Player

    /**
     * Executes the provided [Runnable] after registering the specified
     * event listener.
     * This 'one-shot' listener behaves like a regular Sponge event listener,
     * with the following differences:
     *
     * - Any exceptions thrown by the handler are treated as a test failure. This
     * allows you to use [Assert] and friends as usual.
     * - If the handler is not invoked at some point during the [Runnable]'s
     * execution, the test fails.
     * - The event handler is automatically unregistered after the [Runnable]
     * completes.
     *
     * This method is useful for verifying that certain client interaction (e.g. clicking
     * a block) causes a Sponge event to be fired.
     *
     *
     * @param eventClass The event class to listen for
     * @param listener The event listener to register
     * @return The event listener that was registerd with Sponge. This is *not* the same
     * as {@param listener}. If you want to unregister your listener early, use the return
     * value of this method with [EventManager.unregisterListeners].
     */
    @Throws(Throwable::class)
    fun <T: Event> listenOneShot(runnable: Runnable, vararg listeners: StandaloneEventListener<T>)

    /**
     * Like {@link #listenOneShot}, but desgined for @CoroutineTest methods
     */
    suspend fun <T: Event> listenOneShotSuspend(block: suspend () -> Unit, vararg listeners: StandaloneEventListener<T>)

    /**
     * Registers an event listener. This behaves like a regular Sponge event listener,
     * with one difference
     *
     * - Any exceptions thrown by the handler are treated as a test failure. This
     * allows you to use [Assert] and friends as usual.
     *
     * It us usually preferrable to use either this method or [.listenOneShot]
     * instead of using [EventManager] directly. These methods ensure that any exceptions
     * thrown by listeners result in a test failure. Any exceptions thrown
     * by normal listeners are logged, but otherwise ignored.
     *
     * This method is useful for verifying that certain client interaction (e.g. clicking
     * a block) causes a Sponge event to be fired.
     *
     * If you want to unregister this listener, make sure to use the returned [StandaloneEventListener],
     * **not** the oen you passed as a parameter.
     *
     * @param listener The event listener to register
     * @return The event listener that was registerd with Sponge. This is *not* the same
     * as {@param listener}. If you want to unregister your listener, use the return
     * value of this method with [EventManager.unregisterListeners].
     */
    fun <T : Event> listen(listener: StandaloneEventListener<T>): StandaloneEventListener<T>

    /**
     * Registers the specified event listener, and waits at most {@param ticks} for it to fire.
     *
     *
     * This method can be though of as a combination of [.listenOneShot]
     * and [.sleepTicks]. Like [.listenOneShot],
     * this method will cause a test failure if the provided listener does not execute.
     * However, up to and including {@param ticks} ticks are allowed to elapse for the listener
     * runs.
     *
     *
     * If the listener executes before the specified number of ticks have elapsed,
     * this method will return early. The number of remaining ticks will be returned.
     *
     *
     * The specified listener is automatically unregistered before this method returns./p>
     *
     * @param eventClass The class of the event to listen for
     * @param listener The listener to register
     * @param ticks The maximum number of ticks to wait for the listener to fire
     * @return The number of ticks remaining when the listener returned. This will be at most,
     * {@param ticks}, and at least 0. Note that this is an estimate.
     */
    @Throws(Throwable::class)
    fun <T : Event> listenTimeout(runnable: Runnable, listener: StandaloneEventListener<T>, ticks: Int): Int

    /**
     * The same as {@link #listenTimeoet}, but desigend for @CoroutineTest methods
     *
     * <p>Unlike {@link #listenTimeout}, this methods suspends the coroutine until the listener
     * execute or the time runs out, instead of blocking.</p>
     */
    suspend fun <T: Event> listenTimeOutSuspend(block: suspend () -> Unit, listener: StandaloneEventListener<T>, ticks: Int): Int

    /**
     * Runs a [Callable] on the main thread, returning this result.
     *
     *
     * All SpongeAPI methods **must** be called from a runOnMainThread block,
     * or from an event listener.
     *
     *
     * Calls to [Client] methods, as well as [.sleepTicks], must
     * not be called on the main thread. Attempting to call them within the [Callable]
     * passed to this method will throw an exception
     *
     * @param callable The [Callable] to run on the main thread
     * @return The result of the callable
     * @throws Throwable any exception thrown by the Callable
     */
    @Throws(Throwable::class)
    fun <T> runOnMainThread(callable: Callable<T>): T

    /**
     * Runs a [Runnable] on the main threa
     *
     *
     * Calls to [Client] methods, as well as [.sleepTicks], must
     * not be called on the main thread. Attempting to call them within the [Runnable]
     * passed to this method will throw an exception
     *
     * @param callable The [Runnable] to run on the main thread
     * @throws Throwable any exception thrown by the Callable
     */
    @Throws(Throwable::class)
    fun runOnMainThread(callable: Runnable)


    /**
     * Delays the test thread for the specified number of ticks.
     *
     *
     * This does **not** block the main thread. Instead, it blocks
     * the test thread until the specified number of ticks have elapsed
     * on the main thread.
     *
     *
     * If you are trying to wait for updates to propagate to the client,
     * it is preferred to use a dedicated method such as [.waitForInventoryPropagation],
     * instead of trying to determine the necessary number of ticks yourself.
     *
     * @param ticks The number of ticks to wait for
     */
    fun sleepTicks(ticks: Int)

    /**
     * Waits for any server-side inventory updates to propagate to the client.
     *
     *
     * Certain Sponge methods, such as those that modify player inventories, do not
     * immediately cause packets to be sent to the client. Instead, any changes will be detected
     * and sent several ticks later.
     *
     * To avoid race conditions, this method should be called in between
     * any modification to the player's inventory (e.g. player.getRawInventory().offer())
     * and any methods on Client that depend on the server-side change.
     *
     * For example, consider the following code:
     *
     * ItemStack serverStack = ItemStack.of(ItemTypes.GOLD_INGOT, 5);
     * player.getRawInventory().clear();
     * player.getRawInventory().offer();
     *
     * ItemStack clientStack = client.getItemInHand(HandTypes.MAIN_HAND);
     * Assert.assertTrue(ItemStackComparators.ALL.compare(serverStack, clientStack) == 0);
     *
     * Assert.assertTrue() is not guaranteed to succeed. This is because the client's local inventory
     * may not yet have been updated to reflect the server-side inventory.
     * To ensure that the test always succeeds, it should be modified to look like this:
     *
     * ...
     * testUtils.waitForInventoryPropagation();
     * Assert.assertTrue(ItemStackComparators.ALL.compare(serverStack, clientStack) == 0);
     *
     */
    fun waitForInventoryPropagation()

    /**
     * Waits for any server-side entity spawns to propagate to the client.
     *
     *
     * This method should be used after
     *
     * See [.waitForInventoryPropagation] for more details.
     */
    fun waitForEntitySpawn()

    /**
     * Waits for the longest-possible propagation to occur.
     *
     *
     * This method can be used when combining two actions
     * that each have a corresponding 'wait' method.
     *
     * For example, setting an item in the player's inventory and
     * spawning a cow would normally require two separate wait calls
     * - one to [.waitForInventoryPropagation], and one to
     * [.waitForEntitySpawn]. Instead, [.waitForAll]}
     * can be used to ensure that any pending updates have been
     * sent to the client.
     */
    fun waitForAll()

    /**
     * Waits for all of the necessary chunk packets to be sent to the player.
     *
     * <p>Calling this is only necessary if you test explicitly switches the players'
     * world via setBLocation. In that case, it should be called immediately after the
     * setLocation call.</p>
     *
     * <p>If you don't switch the player's world from your test code, you don't
     * need to call this method - it will be called for you before your test starts</p>
     */
    fun waitForWorldChunks()


    /**
     * The same as {@link #waitForWorldChunks}, but designed
     * for a @CoroutineTest.
     */
    suspend fun waitForWorldChunksSuspend()

    /**
     * Checks that the two ItemStacks are equal when compared with ItemStackComparators.ALL.
     * If they are not equal, an AssertionError is thrown with an informative message.
     */
    fun assertStacksEqual(serverStack: ItemStack, clientStack: ItemStack)
}
