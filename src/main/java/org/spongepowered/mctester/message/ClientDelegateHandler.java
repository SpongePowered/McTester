package org.spongepowered.mctester.message;

import org.spongepowered.mctester.message.toclient.BaseClientMessage;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

public class ClientDelegateHandler implements MessageHandler<BaseClientMessage> {

    @Override
    public void handleMessage(BaseClientMessage message, RemoteConnection connection, Platform.Type side) {
        /*Minecraft.getMinecraft().addScheduledTask(new Runnable() {

            @Override
            public void run() {
                message.process();
            }
        });*/
        message.process();

    }
}
