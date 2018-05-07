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
import org.spongepowered.mctester.junit.RunnerEvents;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

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

            @Override
            public void run() {
                Minecraft.getMinecraft().displayGuiScreen(new GuiScreen() {

                    private int ticks;

                    @Override
                    public void updateScreen() {
                        // Let a few ticks go by, to ensure that the game is actually paused
                        // when we shut down the server. If the game isn't actually
                        // paused, we'll run into a nasty race condition
                        if (ticks++ != 5) {
                            return;
                        }

                        // We could forcibly pause the game ourselves, but it's better
                        // to mimic Vanilla as closely as possible, to minimize any
                        // unforseen problems.
                        if (!Minecraft.getMinecraft().isGamePaused()) {
                            throw new IllegalStateException("The game didn't pause for some reason");
                        }

                        if (Minecraft.getMinecraft().isIntegratedServerRunning()) {
                            // This seems completely uncessary, and is just asking for race conditions
                            Minecraft.getMinecraft().world.sendQuittingDisconnectingPacket();
                            Minecraft.getMinecraft().loadWorld(null);

                        }

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
