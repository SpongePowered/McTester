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
import org.spongepowered.mctester.internal.internal.TerminateVM;

public class ForceShutdownHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        if (throwable instanceof FMLSecurityManager.ExitTrappedException) {
            // Try and stop us now, FML!
            System.err.println("FMLSecurityManager tried to stop VM from exiting, bypassing...");
            // Unfortunately, we have no way of getting the real error code.
            // Since this class should only be used in a test environment, returing '0'
            // shouldn't be *too* bad - any test errors should have been recorded separately.
            TerminateVM.terminate("net.minecraftforge.fml", 0);
            //this.doShutdown(0);
        }
    }

    private void doShutdown(int code) {
        try {
            Class.forName("java.lang.Shutdown").getDeclaredMethod("exit", int.class).invoke(null, code);
        } catch (Exception e) {
            // what can i do
            System.err.println("Unable to forcibly shutdown the VM. Giving up and throwing ThreadDeath");
            e.printStackTrace();
            throw new ThreadDeath();
        }
    }
}
