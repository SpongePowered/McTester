package pw.aaron1011.mctester;

import com.google.inject.Inject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import org.slf4j.Logger;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import pw.aaron1011.mctester.framework.TesterHandler;
import pw.aaron1011.mctester.framework.TesterThread;
import pw.aaron1011.mctester.framework.proxy.SpongeProxy;
import pw.aaron1011.mctester.message.BaseClientHandler;
import pw.aaron1011.mctester.message.BaseServerHandler;
import pw.aaron1011.mctester.message.ClientDelegateHandler;
import pw.aaron1011.mctester.message.ServerDelegateHandler;
import pw.aaron1011.mctester.message.toclient.MessageChat;
import pw.aaron1011.mctester.message.toserver.MessageAck;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
    public volatile TesterThread testerThread = new TesterThread();

    public McTester() {
        INSTANCE = this;
    }

    public static void ack() {
        McTester.INSTANCE.channel.sendToServer(new MessageAck());
    }


    @Listener
    public void onInit(GameInitializationEvent event) {
        this.channel = Sponge.getChannelRegistrar().createChannel(this, "mctester");

        ClientDelegateHandler clientDelegateHandler = new ClientDelegateHandler();
        ServerDelegateHandler serverDelegateHandler = new ServerDelegateHandler();

        this.channel.registerMessage(MessageAck.class, 0, Platform.Type.SERVER, (MessageHandler) serverDelegateHandler);

        this.channel.registerMessage(MessageChat.class, 1, Platform.Type.CLIENT, (MessageHandler) clientDelegateHandler);
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
        Sponge.getCommandManager().register(this, CommandSpec.builder().executor(new CommandExecutor() {

            @Override
            public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
                McTester.this.handler.run();
                src.sendMessage(Text.of(TextColors.GREEN, "Here we go!"));

                return CommandResult.success();
            }
        }).build(),
                "runTest");
    }

    public static boolean shouldProxy(Object object) {
        return object.getClass().getName().startsWith("org.spongepowered") && !(object.getClass().getName().equals(SpongeProxy.class.getName()));
    }

    public static Object proxy(Object object) {
        if (McTester.shouldProxy(object)) {
            return Proxy.newProxyInstance(McTester.class.getClassLoader(), getAllInterfaces(object.getClass()), new SpongeProxy(object));
        }
        return object;
    }

    private static Class<?>[] getAllInterfaces(Class<?> clazz) {
        List<Class<?>> interfaces = new ArrayList<>();
        while (!clazz.equals(Object.class)) {
            interfaces.addAll(Arrays.asList(clazz.getInterfaces()));
            clazz = clazz.getSuperclass();
        }

        Class<?>[] interfacesFinal = new Class[interfaces.size()];
        interfaces.toArray(interfacesFinal);

        return interfacesFinal;
    }



}
