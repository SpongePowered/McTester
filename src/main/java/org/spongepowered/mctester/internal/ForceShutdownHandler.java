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

import net.minecraftforge.fml.relauncher.FMLSecurityManager;
import org.spongepowered.mctester.api.RunnerEvents;

// We want JUnit to shut down the VM, not Minecraft, so
// we need to bypass FMLSecurityManager

// ForceShutdownHandler intercepts ExitTrappedException that will eventually be thrown
// by JUnit attemping to shut down the VM. It forcible terminates the VM
// through our TerminateVM class, which bypasses FMLSecurityManager

// We keep the game open if at least one test doesn't want to shut it down
        /*if (this.options.exitMinecraftOnFinish()) {
            this.shutDownMinecraft();
        }*/
public class ForceShutdownHandler implements Thread.UncaughtExceptionHandler {

    private final TestStatus testStatus;

    public ForceShutdownHandler(TestStatus testStatus) {
        this.testStatus = testStatus;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        if (throwable instanceof FMLSecurityManager.ExitTrappedException) {
            // Try and stop us now, FML!
            System.err.println("FMLSecurityManager tried to stop VM from exiting, bypassing...");

            if (testStatus.succeeded()) {
                if (!RealJUnitRunner.GLOBAL_SETTINGS.shutdownOnSuccess()) {
                    this.waitForClose("tests succeeded");
                }
                this.doShutdown(0);
            } else if (testStatus.failed()) {
                if (!RealJUnitRunner.GLOBAL_SETTINGS.shutdownOnFailure()) {
                    this.waitForClose("tests failed");
                }
                this.doShutdown(-1);
            }
            /*} else if (testStatus.errored()) {
                if (!RealJUnitRunner.GLOBAL_SETTINGS.shutdownOnError()) {
                    this.waitForClose("JUnit unexpecetedly errored");
                }
                this.doShutdown(-2);
            }*/
        } else {
            System.err.println("Uncaught exception!!!");
            throwable.printStackTrace();
        }
    }

    private void waitForClose(String message) {
        System.err.println("Waiting for Minecraft to close because " + message);
        RunnerEvents.waitForGameClosed();
    }

    private void doShutdown(int code) {
        // Unfortunately, we have no way of getting the real fakeError code.
        TerminateVM.terminate("net.minecraftforge.fml", code);
    }
}
