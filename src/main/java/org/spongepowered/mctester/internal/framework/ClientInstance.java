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
package org.spongepowered.mctester.internal.framework;

import net.minecraft.launchwrapper.LaunchClassLoader;
import org.spongepowered.mctester.api.junit.MinecraftClientStarter;
import org.spongepowered.mctester.api.junit.MinecraftRunner;
import org.spongepowered.mctester.api.junit.RunnerEvents;
import org.spongepowered.mctester.api.junit.clientmanager.WrapperClassLoader;

import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.Map;

public class ClientInstance {

    private WrapperClassLoader classLoader;
    private MinecraftClientStarter starter;
    private Map<String, String> resolvedLibraries;
    private static int nextId = 1;
    private int id;


    public ClientInstance(Map<String, String> resolvedLibraries) {
        this.id = nextId++;
        this.resolvedLibraries = resolvedLibraries;
        URLClassLoader classLoader = (URLClassLoader) MinecraftRunner.rootClassLoader;
        if (classLoader.getClass().getName().equals("net.minecraft.launchwrapper.LaunchClassLoader")) {
            throw new IllegalStateException("ClientInstance created from LaunchClassLoader!");
        }
        this.classLoader = new WrapperClassLoader(classLoader.getURLs(), (LaunchClassLoader) RunnerEvents.getLaunchClassLoader(MinecraftRunner.rootClassLoader),
                MinecraftRunner.GLOBAL_SETTINGS.getRootLibDir(), this.id, this.resolvedLibraries);
        RunnerEvents.initNewInstance(this.classLoader);
        this.starter = new MinecraftClientStarter(this.classLoader);
        starter.startClient(id);
    }

    public void shutdown() {
        try {
            ClassLoader launchClassLoader = RunnerEvents.getLaunchClassLoader(this.classLoader);
            Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft", true, launchClassLoader);

            Method getMinecraft = minecraftClass.getMethod("getMinecraft");
            Method shutdown = minecraftClass.getMethod("shutdown");

            Object minecraft = getMinecraft.invoke(null);

            if (minecraft != null) {
                shutdown.invoke(minecraft);
            }
        } catch (Throwable e) {
            System.err.println("Shutdown failure!");
            e.printStackTrace();
            throw new Error("Failed to shutdown Minecraft client!", e);
        }
    }

}
