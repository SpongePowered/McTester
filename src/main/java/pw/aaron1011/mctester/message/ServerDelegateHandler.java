package pw.aaron1011.mctester.message;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;
import pw.aaron1011.mctester.message.toserver.BaseServerMessage;

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
