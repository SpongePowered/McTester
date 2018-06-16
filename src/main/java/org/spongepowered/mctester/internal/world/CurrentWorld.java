package org.spongepowered.mctester.internal.world;

import com.google.common.util.concurrent.Futures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.ISaveFormat;
import org.spongepowered.mctester.internal.interfaces.IMixinMinecraft;
import org.spongepowered.mctester.junit.RunnerEvents;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class CurrentWorld {

    private String name;

    public void joinNewWorld(String baseName) {
        this.exitToMainMenu();

        RunnerEvents.resetPlayerJoined();

        long seed = new Random().nextLong();
        this.name = baseName + String.valueOf(seed).substring(0, 5);
        System.err.println("Creating world " + this.name);

        Futures.getUnchecked(Minecraft.getMinecraft().addScheduledTask(new Runnable() {

            @Override
            public void run() {

                WorldSettings worldsettings = new WorldSettings(seed, GameType.CREATIVE, false, false, WorldType.FLAT);
                worldsettings.enableCommands();
                Minecraft.getMinecraft().launchIntegratedServer(CurrentWorld.this.name, CurrentWorld.this.name, worldsettings);
            }
        }));

        RunnerEvents.waitForPlayerJoin();
    }

    public void deleteWorld() {
        System.err.println("Deleting world: " + this.name);
        this.exitToMainMenu();
        this.deleteInternal();
    }

    private void deleteInternal() {
        ISaveFormat isaveformat = Minecraft.getMinecraft().getSaveLoader();
        isaveformat.flushCache();
        isaveformat.deleteWorldDirectory(this.name);
    }


    public void exitToMainMenu() {
        ((IMixinMinecraft) Minecraft.getMinecraft()).setAllowPause(true);

        if (!Minecraft.getMinecraft().isIntegratedServerRunning()) {
            RunnerEvents.resetPlayerJoined();
            return;
        }

        // We deliberately avoid using Minecraft.addScheduledTask here.
        //
        // When we call World.sendQuittingDisconnectingPacket,
        // the client thread will block waiting for the Netty channel
        // to close.
        //
        // The client controls all access to its scheduledTasks list -
        // both adding and executing tasks - with
        // a synchronized(Minecraft.scheduledTasks) block
        //
        // If we call sendQuittingDisconnectingPacket in a scheduled
        // task, we'll end up blcoking on the channel closure while
        // in a synchronized(Minecraft.scheduledTasks). However, the
        // Netty client thread can also enter a synchronized(Minecraft.scheduledTasks)
        // block, since it schedulers packet handlers on the main thread.
        // This means that we can very easily end up with a deadlock
        // if the Netty thread tries to process a packet while we're
        // waiting for the channel to close
        //
        // To avoid the deadlock, we do what Vanilla does - call
        // sendQuittingDisconnectingPacket from a GUI.
        // In Vanilla, this is either a GuiGameOver or a GuiInGameOMenu
        // Here, we create a simple GuiScreen subclass to run our code
        // on the client thread.
        CompletableFuture<Void> atMainMenu = new CompletableFuture<>();
        Minecraft.getMinecraft().addScheduledTask(new Runnable() {

            private Future<Void> mainThreadTask;
            private int gameLoopRuns = 0;

            @Override
            public void run() {
                Minecraft.getMinecraft().displayGuiScreen(new GuiScreen() {

                    @Override
                    public void updateScreen() {
                        // Minecraft only pauses the game in Minecraft#runGameLoop.
                        // However, the game (and therefore any open GuiScreen) ticks multiple
                        // times per call to runGameLoop.

                        // To ensure that the game is actually paused, we schedule a new task
                        // from our GuiScreen, and don't do anything until it runs.
                        // Since all client scheduled tasks run at the start of
                        // runGameLoop, we know that we've gone through a full
                        // execution of runGameLoop when our second scheduled task runs
                        // (the gui first opens in the previous set of runGameLoop
                        // scheduled tasks).

                        // All of this ensures that we only try to exit the game when
                        // it's actually paused, and lets us avoid taking over
                        // Vanilla's handling of pausing the game. Just in case,
                        // we throw an exception if the game isn't actually paused - however,
                        // this should never happen.
                        if (gameLoopRuns < 1) {
                            if (mainThreadTask == null) {
                                mainThreadTask = ((IMixinMinecraft) Minecraft.getMinecraft()).addScheduledTaskAlwaysDelay(() -> {
                                    gameLoopRuns++;
                                    return null;
                                });
                            }
                            return;
                        }

                        // We could forcibly pause the game ourselves, but it's better
                        // to mimic Vanilla as closely as possible, to minimize any
                        // unforseen problems.
                        if (!Minecraft.getMinecraft().isGamePaused() && Minecraft.getMinecraft().isIntegratedServerRunning()) {
                            throw new IllegalStateException("The game didn't pause for some reason");
                        }

                        if (Minecraft.getMinecraft().world != null) {
                            Minecraft.getMinecraft().world.sendQuittingDisconnectingPacket();

                        }

                        Minecraft.getMinecraft().loadWorld(null);
                        Minecraft.getMinecraft().displayGuiScreen(new GuiMainMenu());
                        atMainMenu.complete(null);
                    }
                });
            }
        });

        Futures.getUnchecked(atMainMenu);
        RunnerEvents.resetPlayerJoined();
    }

}
