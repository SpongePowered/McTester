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

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.gradle.internal.impldep.org.testng.collections.Lists;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.mctester.internal.RemoveSecurityManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

public class MinecraftRunnerTweaker implements ITweaker {

    public MinecraftRunnerTweaker() {
        // This is your fault, LaunchWrapper, not mine. You drove me to this
        // We wait until LaunchClassLoader has started the main class before loading classes.
        // However, transformers can still be registered in the future. To avoid any possiblility
        // of ConcurrentModificationExceptions, we install a thread-safe List implementation.
        try {
            System.err.println("Hacking LaunchClassLoader to make 'transformers' thread-safe");
            Field transformersField = LaunchClassLoader.class.getDeclaredField("transformers");
            CopyOnWriteArrayList<IClassTransformer> newTransformers = new CopyOnWriteArrayList<>();
            transformersField.setAccessible(true);
            transformersField.set(Launch.classLoader, newTransformers);
        } catch (Throwable e) {
            System.err.println("Exception while trying to fix LaunchClassLoader!");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        RemoveSecurityManager.clearSecurityManager();
    }

    private List<String> getCustomMixins() {
        Properties properties = new Properties();
        try(InputStream stream = getClass().getClassLoader().getResourceAsStream("META-INF/mctester/custom-mixins.properties")) {
            if (stream == null) {
                return Lists.newArrayList();
            }
            properties.load(stream);
            String mixins = properties.getProperty("mixins");
            if (mixins != null) {
                return Arrays.asList(mixins.split(","));
            }
            return Lists.newArrayList();
        } catch (IOException e) {
            throw new RuntimeException("Error with custom resource stream!", e);
        }
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        classLoader.addClassLoaderExclusion("org.spongepowered.mctester.api.junit");
        // This is *VERY* important, because without this
        // JUnit will be vey unhappy when in MinecraftTestRunner
        // we replace the class under test with the one from the
        // Minecraft ClassLoader instead of the parent one into
        // which JUnit Framework classes were already loaded
        classLoader.addClassLoaderExclusion("junit.");
        classLoader.addClassLoaderExclusion("org.junit.");
        classLoader.addClassLoaderExclusion("org.hamcrest");
        classLoader.addClassLoaderExclusion("kotlin");


        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.mctester.json");
        for (String config: getCustomMixins()) {
            Mixins.addConfiguration(config);
        }
    }

    @Override
    public String getLaunchTarget() {
        return null;
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }
}
