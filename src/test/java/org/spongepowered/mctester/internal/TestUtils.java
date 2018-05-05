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

import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;

import java.util.concurrent.Callable;

public interface TestUtils {

    /**
     * Registers a 'one-shot' listener. This behaves like a regular Sponge event listener,
     * with the following differences:
     *
     *
     * @param eventClass
     * @param listener
     * @param <T>
     */
    <T extends Event> void listenOneShot(Class<T> eventClass, EventListener<? super T> listener);

    <T> T batchActions(Callable<T> callable) throws Throwable;

    void batchActions(Runnable callable) throws Throwable;

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
}
