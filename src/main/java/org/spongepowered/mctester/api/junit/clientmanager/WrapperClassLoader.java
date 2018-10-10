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
package org.spongepowered.mctester.api.junit.clientmanager;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Set;

public class WrapperClassLoader extends URLClassLoader {

    public static final String LAUNCH_NAME = "net.minecraft.launchwrapper.Launch";

    // These are the only classes we want to load in this classloader -
    // everything gets passes up to the parent
    // This allows us to create an isolated LaunchClassLoader,
    // which will in turn create a completely isolated game instance.
    private static final List<String> toLoad = Lists.newArrayList(
            "GradleStart",
            "GradleStartServer",
            "GradleStartCommon",
            "net.minecraft.launchwrapper",
            "org.spongepowered.mctester.api.junit.clientmanager"
    );

    public WrapperClassLoader(URL[] urls) {
        super(urls, WrapperClassLoader.class.getClassLoader());
    }

    private boolean shouldLoad(String name) {
        for (String load: toLoad) {
            if (name.startsWith(load)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        /*synchronized (this.getClassLoadingLock(name)) {
            Class<?> clazz = this.findLoadedClass(name);
            if (clazz == null) {
                if (this.shouldLoad(name)) {
                    clazz = this.findClass(name);
                } else {
                    clazz = this.getParent().loadClass(name);
                }
            }
            if (resolve) {
                this.resolveClass(clazz);
            }
            return clazz;
        }*/

        // Take two - let's try loading *everything* in this classloader,
        // as long as w can find a URL for it
        synchronized (this.getClassLoadingLock(name)) {
            Class<?> clazz = this.findLoadedClass(name);

            boolean fromRT = this.fromRTJar(name);
            boolean shouldParent = this.shouldUseParent(name);

            if (clazz == null && !fromRT && !shouldParent /*!name.startsWith("java") && !name.startsWith("sun") && !name.startsWith("org.xml")*/) {
                try {
                    clazz = this.findClass(name);
                } catch (ClassNotFoundException e) {
                }
            }

            // If didn't have any luck with findClass,
            // check the parent
            if (clazz == null) {
                clazz = this.getParent().loadClass(name);
            }

            if (resolve) {
                this.resolveClass(clazz);
            }
            return clazz;
        }
    }

    boolean shouldUseParent(String className) {
        return className.equals("org.spongepowered.mctester.api.junit.RunnerEvents");
    }

    boolean fromRTJar(String className) {
        URL url = this.findResource(className.replace(".", "/").concat(".class"));
        if (url == null) {
            return false;
        }
        return url.getPath().split("!")[0].endsWith("rt.jar");
    }
}
