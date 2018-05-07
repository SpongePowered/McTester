package org.spongepowered.mctester.internal.message;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.network.Message;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

public abstract class BaseServerHandler<T extends Message> implements MessageHandler<T> {

    @Override
    public final void handleMessage(T message, RemoteConnection connection, Platform.Type side) {

        // We deliberately avoid using Sponge's scheduler API here.
        // All Sponge synchronous tasks run at the start of the tick (TickEvent.Phase.START)
        // which is *before* any packet scheduled tasks.

        // If an ACK packet arrives after another packet, but during the same tick,
        // we need to ensure that their respective handlers run on the main thread
        // in the proper order. If we were to use the Sponge scheduler API, our ACK
        // packet handler would run *before* the other packet handler, even though
        // it arrived *after*
        ((MinecraftServer) Sponge.getServer()).addScheduledTask(new Runnable() {
            @Override
            public void run() {
                BaseServerHandler.this.properHandleMessage(message, connection, side);
            }
        });
    }

    protected abstract void properHandleMessage(T message, RemoteConnection connection, Platform.Type side);
}
