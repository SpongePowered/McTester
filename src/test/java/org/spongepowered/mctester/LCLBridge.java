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
package org.spongepowered.mctester;

import org.spongepowered.mctester.junit.MinecraftRunner;
import org.spongepowered.mctester.old.appclass.ErrorSlot;
import org.spongepowered.mctester.old.framework.TesterManager;
import net.minecraft.client.Minecraft;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Method;

public class LCLBridge {

    private MinecraftRunner runner;
    private Class<?> testerManagerClass;
    private Object testerManager;
    private Object testerManagerFakeGame;
    private Object testerManagerClient;

    private Class<?> minecraftClass;
    private Method minecraftShutdown;
    private Object minecraft;

    private ErrorSlot errorSlot;

    public LCLBridge(MinecraftRunner runner) {
        this.runner = runner;

        /*try {
            this.testerManagerClass = Class.forName(TesterManager.class.getName(), true, this.runner.starter.getMinecraftServerClassLoader());
            this.testerManager = this.testerManagerClass.getConstructor().newInstance();

            this.testerManagerFakeGame = this.testerManagerClass.getField("fakeGame").get(this.testerManager);
            this.testerManagerClient = this.testerManagerClass.getField("client").get(this.testerManager);

            this.minecraftClass = Class.forName(Minecraft.class.getName(), true, this.runner.starter.getMinecraftServerClassLoader());
            this.minecraftShutdown = this.minecraftClass.getMethod("shutdown");
            this.minecraft = this.minecraftClass.getMethod("getMinecraft").invoke(null);

            this.errorSlot = (ErrorSlot) this.testerManagerClass.getField("errorSlot").get(this.testerManager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }*/
    }

    public Object createTest(TestClass testClass) {
        try {
            return testClass.getOnlyConstructor().newInstance(this.testerManagerFakeGame, this.testerManagerClient, this.testerManager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void shutDownMinecraft() {
        try {
            // We want JUnit to shut down the VM, not Minecraft, so
            // we need to bypass FMLSecurityManager

            // ForceShutdownHandler intercepts ExitTrappedException that will eventually be thrown
            // by JUnit attemping to shut down the VM. It forcible terminates the VM
            // through our TerminateVM class, which bypasses FMLSecurityManager
            Thread.setDefaultUncaughtExceptionHandler(new ForceShutdownHandler());
            this.minecraftShutdown.invoke(this.minecraft);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ErrorSlot getErrorSlot() {
        return this.errorSlot;
    }


    // Looks up the corresponding class in LaunchClassLoader,
    // and uses the LaunchClassLoader class to instantiate a new
    // object
    /*private Object createInLCL(Class<?> clazz) {
        try {
            Class<?> lcsClazz = Class.forName(clazz.getName(), true, this.runner.starter.getMinecraftServerClassLoader());
            return (T) lcsClazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/

}
