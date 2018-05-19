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
package org.spongepowered.mctester.internal;

import org.junit.Assert;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.mctester.internal.event.StandaloneEventListener;
import org.spongepowered.mctester.internal.framework.Client;
import org.spongepowered.mctester.junit.MinecraftRunner;

import java.util.concurrent.Callable;

/**
 * Helper utilities useful in writing tests against Minecraft
 */
public interface TestUtils {

    /**
     * Gets a special {@link Game} suitable for use in a test
     *
     * <p>You **MUST NOT** use {@link Sponge#getGame()} in your test.
     * All tests that use {@link MinecraftRunner} are run on a separate thread.
     * {@link TestUtils#getGame()} creates a special proxy object
     * that transparently executes all functions calls (both for
     * {@link Game} and returned objects</p>
     *
     * @return
     */
    Game getGame();

    /**
     * Returns the main interface for controlling the client.
     *
     * <p>Unlike normal SpongeAPI methods, all methods in this class operate directly
     * on the client thread. They mimic, as closely as possible, the actions
     * of a human interacting with the Minecraft client.</p>
     *
     * <p>To control the client using the normal SpongeAPI methods, use {@link #getThePlayer()}</p>
     *
     * @return
     */
    Client getClient();

    /**
     * Gets the player connected to the server.
     *
     * <p>This is a normal SpongeAPI {@link Player}. This
     * method is a shortcut to get the one player connected to
     * the server, since only one player is connected in the test
     * environment.</p>
     * @return
     */
    Player getThePlayer();

    /**
     * Executes the provided {@link Runnable} after registering the specified
     * event listener.
     * This 'one-shot' listener behaves like a regular Sponge event listener,
     * with the following differences:
     *
     * - Any exceptions thrown by the handler are treated as a test failure. This
     *   allows you to use {@link Assert} and friends as usual.
     * - If the handler is not invoked at some point during the {@link Runnable}'s
     * execution, the test fails.
     * - The event handler is automatically unregistered after the {@link Runnable}
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
     * value of this method with {@link EventManager#unregisterListeners(Object)}.
     */
    void listenOneShot(Runnable runnable, StandaloneEventListener<?>... listeners) throws Throwable;

    /**
     * Registers an event listener. This behaves like a regular Sponge event listener,
     * with one difference
     *
     * - Any exceptions thrown by the handler are treated as a test failure. This
     *   allows you to use {@link Assert} and friends as usual.
     *
     * It us usually preferrable to use either this method or {@link #listenOneShot(Class, EventListener, Runnable)}
     * instead of using {@link EventManager} directly. These methods ensure that any exceptions
     * thrown by listeners result in a test failure. Any exceptions thrown
     * by normal listeners are logged, but otherwise ignored.
     *
     * This method is useful for verifying that certain client interaction (e.g. clicking
     * a block) causes a Sponge event to be fired.
     *
     * If you want to unregister this listener, make sure to use the returned {@link EventListener},
     * **not** the oen you passed as a parameter.
     *
     * @param eventClass The event class to listen for
     * @param listener The event listener to register
     * @return The event listener that was registerd with Sponge. This is *not* the same
     * as {@param listener}. If you want to unregister your listener, use the return
     * value of this method with {@link EventManager#unregisterListeners(Object)}.
     */
    <T extends Event> EventListener<T> listen(Class<T> eventClass, EventListener<? super T> listener);

    /**
     * Registers the specified event listener, and waits at most {@param ticks} for it to fire.
     *
     * <p>This method can be though of as a combination of {@link #listenOneShot(Class, EventListener)}
     * and {@link #sleepTicks(int)}. Like {@link #listenOneShot(Class, EventListener)},
     * this method will cause a test failure if the provided listener does not execute.
     * However, up to and including {@param ticks} ticks are allowed to elapse for the listener
     * runs.</p>
     *
     * <p>If the listener executes before the specified number of ticks have elapsed,
     * this method will return early. The number of remaining ticks will be returned.</p>
     *
     * <p>The specified listener is automatically unregistered before this method returns./p>
     *
     * @param eventClass The class of the event to listen for
     * @param listener The listener to register
     * @param ticks The maximum number of ticks to wait for the listener to fire
     * @return The number of ticks remaining when the listener returned. This will be at most,
     * {@param ticks}, and at least 0. Note that this is an estimate.
     */
    <T extends Event> int listenTimeout(Runnable runnable, StandaloneEventListener<T> listener, int ticks) throws Throwable;

    /**
     * Runs a {@link Callable} on the main thread, returning this result.
     *
     * <p>This method is provided as a performance optimization. It is completely optional:
     * if you don't use it, your code will run on the main thread one method
     * call at a time, instead of all at once.</p>
     *
     * <p>Calls to {@link Client} methods, as well as {@link #sleepTicks(int)}, must
     * not be called on the main thread. Attempting to call them within the {@link Callable}
     * passed to this method will throw an exception</p>
     *
     * @param callable The {@link Callable} to run on the main thread
     * @return The result of the callable
     * @throws Throwable any exception thrown by the Callable
     */
    <T> T batchActions(Callable<T> callable) throws Throwable;

    /**
     * Runs a {@link Runnable} on the main threa
     *
     * <p>This method is provided as a performance optimization. It is completely optional:
     * if you don't use it, your code will run on the main thread one method
     * call at a time, instead of all at once.</p>
     *
     * <p>Calls to {@link Client} methods, as well as {@link #sleepTicks(int)}, must
     * not be called on the main thread. Attempting to call them within the {@link Runnable}
     * passed to this method will throw an exception</p>
     *
     * @param callable The {@link Runnable} to run on the main thread
     * @throws Throwable any exception thrown by the Callable
     */
    void batchActions(Runnable callable) throws Throwable;

    /**
     * Delays the test thread for the specified number of ticks.
     *
     * <p>This does <b>not</b> block the main thread. Instead, it blocks
     * the test thread until the specified number of ticks have elapsed
     * on the main thread.</p>
     *
     * <p>If you are trying to wait for updates to propagate to the client,
     * it is preferred to use a dedicated method such as {@link #waitForInventoryPropagation()},
     * instead of trying to determine the necessary number of ticks yourself.</p>
     *
     * @param ticks The number of ticks to wait for
     */
    void sleepTicks(int ticks);

    /**
     * Waits for any server-side inventory updates to propagate to the client.
     *
     * <p>Certain Sponge methods, such as those that modify player inventories, do not
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
     * </p>
     */
    void waitForInventoryPropagation();

    /**
     * Waits for any server-side entity spawns to propagate to the client.
     *
     * <p>This method should be used after </p>
     * <p>See {@link #waitForInventoryPropagation()} for more details.</p>
     */
    void waitForEntitySpawn();

    /**
     * Waits for the longest-possible propagation to occur.
     *
     * <p>This method can be used when combining two actions
     * that each have a corresponding 'wait' method.
     *
     * For example, setting an item in the player's inventory and
     * spawning a cow would normally require two separate wait calls
     * - one to {@link #waitForInventoryPropagation}, and one to
     * {@link #waitForEntitySpawn()}. Instead, {@link #waitForAll()}}
     * can be used to ensure that any pending updates have been
     * sent to the client.</p>
     */
    void waitForAll();
}
