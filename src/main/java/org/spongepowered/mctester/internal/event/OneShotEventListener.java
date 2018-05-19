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
package org.spongepowered.mctester.internal.event;

import org.spongepowered.mctester.internal.appclass.ErrorSlot;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;

import java.util.concurrent.CompletableFuture;

public class OneShotEventListener<T extends Event> extends ErrorPropagatingEventListener<T> {

    public CompletableFuture<Long> handleStarted = new CompletableFuture<>();
    public CompletableFuture<Void> handleFinished = new CompletableFuture<>();
    public AssertionError fakeError;

    public OneShotEventListener(Class<T> eventClass, EventListener<? super T> listener, ErrorSlot errorSlot) {
        super(eventClass, listener, errorSlot);
    }

    @Override
    public void handle(T event) throws Exception {
        this.handleStarted.complete(System.currentTimeMillis());
        try {
            this.listener.handle(event);
        } catch (Throwable e) {
            this.errorSlot.setErrorIfUnset(e);
            throw e;
        } finally {
            this.handleFinished.complete(null);
        }
    }

    public void throwCaughtException() throws Throwable {
        this.errorSlot.throwIfSet();
    }
}
