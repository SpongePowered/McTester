package org.spongepowered.mctester.old.message;

import net.minecraft.client.Minecraft;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.Message;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

public abstract class BaseClientHandler<T extends Message> implements MessageHandler<T> {

    @Override
    public final void handleMessage(T message, RemoteConnection connection, Platform.Type side) {
        Minecraft.getMinecraft().addScheduledTask(new Runnable() {

            @Override
            public void run() {
                BaseClientHandler.this.properHandleMessage(message, connection, side);
            }
        });
    }

    protected abstract void properHandleMessage(T message, RemoteConnection connection, Platform.Type side);
}
