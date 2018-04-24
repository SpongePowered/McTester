package pw.aaron1011.mctester;

import com.google.inject.Inject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import org.apache.logging.log4j.core.Logger;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.plugin.Plugin;
import pw.aaron1011.mctester.framework.TesterHandler;
import pw.aaron1011.mctester.message.toclient.MessageChat;
import pw.aaron1011.mctester.message.toserver.MessageAck;

import java.lang.reflect.Method;
import java.util.Collection;

@Plugin(
        id = "mctester",
        name = "Mctester",
        description = "A framework for writing fully automated tests of Minecraft",
        authors = {
                "Aaron1011"
        }
)
public class McTester {

    public volatile static McTester INSTANCE;

    @Inject
    public volatile Logger logger;
    public volatile ChannelBinding.IndexedMessageChannel channel;
    public volatile TesterHandler handler = new TesterHandler();

    public McTester() {
        INSTANCE = this;
    }

    @Listener
    public void onInit(GameInitializationEvent event) {
        this.channel = Sponge.getChannelRegistrar().createChannel(this, "mctester");

        this.channel.registerMessage(MessageAck.class, 0, Platform.Type.SERVER, new MessageHandler<MessageAck>() {

            @Override
            public void handleMessage(MessageAck message, RemoteConnection connection, Platform.Type side) {
                McTester.this.handler.receiveAck(message);
            }
        });

        this.channel.registerMessage(MessageChat.class, 1, Platform.Type.CLIENT, new MessageHandler<MessageChat>() {

            @Override
            public void handleMessage(MessageChat message, RemoteConnection connection, Platform.Type side) {
                try {
                    Minecraft minecraft = Minecraft.getMinecraft();
                    minecraft.displayGuiScreen(new GuiChat());

                    GuiChat chat = (GuiChat) minecraft.currentScreen;
                    chat.sendChatMessage(message.message);
                    minecraft.displayGuiScreen(null);

                    McTester.this.channel.sendToServer(new MessageAck());

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        });
    }

    public static Player getThePlayer() {
        Collection<Player> players = Sponge.getServer().getOnlinePlayers();
        if (players.size() != 1) {
            throw new RuntimeException("Unexpected players: " + players);
        }
        return players.iterator().next();
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {

    }

    public static boolean shouldProxy(Object object) {
        return object.getClass().getName().startsWith("org.spongepowered.api");
    }



}
