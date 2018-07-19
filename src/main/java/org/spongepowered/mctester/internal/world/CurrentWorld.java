package org.spongepowered.mctester.internal.world;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.Uninterruptibles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.mctester.api.RunnerEvents;
import org.spongepowered.mctester.internal.interfaces.IMixinMinecraft;
import org.spongepowered.mctester.internal.interfaces.IMixinMinecraftServer;

import java.util.Random;
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

        Futures.getUnchecked(Minecraft.getMinecraft().addScheduledTask(() -> {

            WorldSettings worldsettings = new WorldSettings(seed, GameType.CREATIVE, false, false, WorldType.FLAT);
            worldsettings.enableCommands();
            Minecraft.getMinecraft().launchIntegratedServer(CurrentWorld.this.name, CurrentWorld.this.name, worldsettings);
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
        IMixinMinecraftServer server = (IMixinMinecraftServer) FMLCommonHandler.instance().getSidedDelegate().getServer();
        Thread serverThread = null;
        if (server != null) {
            serverThread = server.getServerMainThread();
        }
        ((IMixinMinecraft) Minecraft.getMinecraft()).setAllowPause(true);

        if (!Minecraft.getMinecraft().isIntegratedServerRunning()) {
            System.err.println("Server has already been stopped, waiting for server thread to exit: " + serverThread);
            if (serverThread != null) {
                Uninterruptibles.joinUninterruptibly(serverThread);
                System.err.println("Server thread shutdown!");
            }
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
                        try {
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
                            System.err.println("Updating fake screen!");
                            if (gameLoopRuns < 1) {
                                System.err.println("Game loop runs < 1");
                                if (mainThreadTask == null) {
                                    System.err.println("We don't have a main thread task yet, creating one");
                                    mainThreadTask = ((IMixinMinecraft) Minecraft.getMinecraft()).addScheduledTaskAlwaysDelay(() -> {
                                        System.err.println("Incrementing game loop runs from minecraft thread");
                                        gameLoopRuns++;
                                        return null;
                                    });
                                }
                                return;
                            }

                            System.err.println("Closing server for real!");

                            // We could forcibly pause the game ourselves, but it's better
                            // to mimic Vanilla as closely as possible, to minimize any
                            // unforseen problems.
                            if (!Minecraft.getMinecraft().isGamePaused() && Minecraft.getMinecraft().isIntegratedServerRunning()) {
                                throw new IllegalStateException("The game didn't pause for some reason");
                            }

                            if (Minecraft.getMinecraft().world != null) {
                                System.err.println("Sending disconnect packet");
                                Minecraft.getMinecraft().world.sendQuittingDisconnectingPacket();

                            }

                            System.err.println("Loading null world...");
                            Minecraft.getMinecraft().loadWorld(null);
                            System.err.println("Displaying main menu...");
                            Minecraft.getMinecraft().displayGuiScreen(new GuiMainMenu());
                            System.err.println("We're done!");
                            atMainMenu.complete(null);
                        } catch (Throwable e) {
                            System.err.println("Caught exception while trying to stop server from fake gui!");
                            e.printStackTrace();
                            throw e;
                        }
                    }
                });
            }
        });


        System.err.println("Waiting on exit to main menu...");
        Futures.getUnchecked(atMainMenu);
        System.err.println("Exited to main menu. Waiing for server thread to stop: " + serverThread);
        if (serverThread != null) {
            Uninterruptibles.joinUninterruptibly(serverThread);
        }
        System.err.println("Server thread has now exited!");
        RunnerEvents.resetPlayerJoined();
    }

}
