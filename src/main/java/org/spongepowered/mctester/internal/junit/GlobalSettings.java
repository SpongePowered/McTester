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
package org.spongepowered.mctester.internal.junit;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class GlobalSettings {

    public static GlobalSettings INSTANCE;

    private File gameDir;
    private File rootLibDir;
    private boolean shutdownOnSuccess;
    private boolean shutdownOnFailure;
    private boolean shutdownOnError;

    public GlobalSettings() {
        try {
            INSTANCE = this;
            this.setGameDir();
            this.shutdownOnSuccess = Boolean.valueOf(System.getProperty("mctester.shutdownOnSuccess", "true"));
            this.shutdownOnFailure = Boolean.valueOf(System.getProperty("mctester.shutdownOnFailure", "true"));
            this.shutdownOnError = Boolean.valueOf(System.getProperty("mctester.shutdownOnError", "true"));
        } catch (Throwable e) {
            System.err.println("Exception creating global settings!");
            e.printStackTrace();
            throw e;
        }
    }

    private void setGameDir() {
        String path = System.getProperty("mctester.gamedir", Paths.get(System.getProperty("java.io.tmpdir"), "mctester", "game").toAbsolutePath().toString());

        File gameDir = new File(path);
        gameDir.mkdirs();
        this.gameDir = gameDir;
        this.rootLibDir = new File(this.gameDir, "clientLibs");
        try {
            FileUtils.deleteDirectory(this.rootLibDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete clientLibs", e);
        }
        if (!this.rootLibDir.mkdir()) {
            throw new RuntimeException("Failed to create " + this.rootLibDir);
        }
    }

    public File getGameDir() {
        return this.gameDir;
    }

    public File getRootLibDir() {
        return this.rootLibDir;
    }

    public boolean shutdownOnSuccess() {
        return this.shutdownOnSuccess;
    }

    public boolean shutdownOnFailure() {
        return shutdownOnFailure;
    }

    public boolean shutdownOnError() {
        return shutdownOnError;
    }
}
