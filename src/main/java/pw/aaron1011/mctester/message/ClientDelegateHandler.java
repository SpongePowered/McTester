package pw.aaron1011.mctester.message;

import net.minecraft.client.Minecraft;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;
import pw.aaron1011.mctester.message.toclient.BaseClientMessage;

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
