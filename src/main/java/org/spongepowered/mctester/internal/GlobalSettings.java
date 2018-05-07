package org.spongepowered.mctester.internal;

import java.io.File;
import java.nio.file.Paths;

public class GlobalSettings {

    public static GlobalSettings INSTANCE;

    private File gameDir;
    private boolean shutdownOnSuccess;
    private boolean shutdownOnFailure;
    private boolean shutdownOnError;

    public GlobalSettings() {
        INSTANCE = this;
        this.setGameDir();
        this.shutdownOnSuccess = Boolean.valueOf(System.getProperty("mctester.shutdownOnSuccess", "true"));
        this.shutdownOnFailure = Boolean.valueOf(System.getProperty("mctester.shutdownOnFailure", "true"));
        this.shutdownOnError   = Boolean.valueOf(System.getProperty("mctester.shutdownOnError", "true"));
    }

    private void setGameDir() {
        String path = System.getProperty("mctester.gamedir", Paths.get(System.getProperty("java.io.tmpdir"), "mctester", "game").toAbsolutePath().toString());

        File gameDir = new File(path);
        gameDir.mkdirs();
        this.gameDir = gameDir;
    }

    public File getGameDir() {
        return this.gameDir;
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
