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
package org.spongepowered.mctester.api;

import com.google.common.util.concurrent.Futures;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class RunnerEvents {

    private static CompletableFuture<Void> clientInitialized = new CompletableFuture<>();
    private static CompletableFuture<Void> playerJoined = new CompletableFuture<>();
    private static CompletableFuture<Void> gameClosed = new CompletableFuture<>();
    private static Map<ClassLoader, CompletableFuture<ClassLoader>> launchClassLoaders = new HashMap<>();
    //private static CompletableFuture<LaunchClassLoader> launchClassLoaderFuture = new CompletableFuture<>();

    public static void waitForPlayerJoin() {
        Futures.getUnchecked(playerJoined);
    }

    public static void setPlayerJoined() {
        playerJoined.complete(null);
    }

    public static void resetPlayerJoined() {
        if (!playerJoined.isDone()) {
            return;
        }
        playerJoined = new CompletableFuture<>();
    }

    public static boolean hasPlayerJoined() {
        return playerJoined.isDone();
    }

    public static void waitForGameClosed() {
        Futures.getUnchecked(gameClosed);
    }

    public static void setGameClosed() {
        gameClosed.complete(null);
    }

    public static void initNewInstance(ClassLoader wrapperClassLoader) {
        CompletableFuture<ClassLoader> old = launchClassLoaders.get(wrapperClassLoader);
        if (old != null) {
            throw new IllegalStateException(String.format("Already registered CompletableFuture %s for wrapper %s", old, wrapperClassLoader));
        }
        launchClassLoaders.put(wrapperClassLoader, new CompletableFuture<>());
    }

    /**
     *
     * @param wrapperClassloader - The class loader used to load a game instance
     * @return
     */
    public static ClassLoader getLaunchClassLoader(ClassLoader wrapperClassloader) {
        Future<ClassLoader> future = launchClassLoaders.get(wrapperClassloader);
        if (future == null) {
            throw new IllegalStateException("Initialization not performed for wrapper we're trying to get " + wrapperClassloader);
        }
        return Futures.getUnchecked(future);
        //return Futures.getUnchecked(launchClassLoaderFuture);
    }

    /**
     * We deliberately set a paremter type of ClassLoader, not LaunchClassLoader.
     * Each game instance will have its own LaunchClassLoader class, loaded by its
     * WrapperClassLoader. If we were to specify a type of LaunchClassLoader in this class,
     * it would not be the same class as any of the game instances use.
     * @param launchClassLoader
     */
    public static void setLaunchClassLoader(ClassLoader launchClassLoader) {
        ClassLoader wrapperClassloader = launchClassLoader.getParent();
        if (!launchClassLoaders.containsKey(wrapperClassloader)) {
            throw new IllegalStateException("Initialization not performed for wrapper " + wrapperClassloader);
        }
        launchClassLoaders.get(wrapperClassloader).complete(launchClassLoader);
    }

    public static void waitForClientInit() {
        Futures.getUnchecked(clientInitialized);
    }

    public static void setClientInit() {
        clientInitialized.complete(null);
    }

    public static void fatalError(Throwable throwable) {
        clientInitialized.completeExceptionally(throwable);
        playerJoined.completeExceptionally(throwable);
        gameClosed.completeExceptionally(throwable);
        for (CompletableFuture<ClassLoader> future: launchClassLoaders.values()) {
            future.completeExceptionally(throwable);
        }
    }

}
