package org.spongepowered.mctester.internal.message;

import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.mctester.internal.message.toserver.BaseServerMessage;

public class ServerDelegateHandler implements MessageHandler<BaseServerMessage> {

    @Override
    public void handleMessage(BaseServerMessage message, RemoteConnection connection, Platform.Type side) {
        message.process();
        /*((MinecraftServer) Sponge.getServer()).addScheduledTask(new Runnable() {

            @Override
            public void run() {
                message.process();
            }
        });*/
    }
}
