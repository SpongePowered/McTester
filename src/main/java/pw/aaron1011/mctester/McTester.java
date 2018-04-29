package pw.aaron1011.mctester;

import com.google.inject.Inject;
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
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import pw.aaron1011.mctester.framework.TesterThread;
import pw.aaron1011.mctester.framework.proxy.RemoteInvocationData;
import pw.aaron1011.mctester.framework.proxy.RemoteInvocationDataBuilder;
import pw.aaron1011.mctester.message.ClientDelegateHandler;
import pw.aaron1011.mctester.message.ServerDelegateHandler;
import pw.aaron1011.mctester.message.toclient.MessageRPCRequest;
import pw.aaron1011.mctester.message.toserver.MessageRPCResponse;

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

    public SpongeExecutorService syncExecutor;

    public volatile ChannelBinding.IndexedMessageChannel channel;

    public McTester() {
        INSTANCE = this;
    }


    @Listener
    public void onInit(GameInitializationEvent event) {
        this.syncExecutor = Sponge.getScheduler().createSyncExecutor(this);
        this.channel = Sponge.getChannelRegistrar().createChannel(this, "mctester");

        ClientDelegateHandler clientDelegateHandler = new ClientDelegateHandler();
        ServerDelegateHandler serverDelegateHandler = new ServerDelegateHandler();

        this.channel.registerMessage(MessageRPCRequest.class, 0, Platform.Type.CLIENT, (MessageHandler) clientDelegateHandler);
        this.channel.registerMessage(MessageRPCResponse.class, 1, Platform.Type.SERVER, (MessageHandler) serverDelegateHandler);

        if (Sponge.getPlatform().getExecutionType().equals(Platform.Type.CLIENT)) {
            Sponge.getDataManager().registerBuilder(RemoteInvocationData.class, new RemoteInvocationDataBuilder(ClientOnly.REAL_CLIENT_HANDLER));
        }

        Sponge.getCommandManager().register(this, CommandSpec.builder().executor(new CommandExecutor() {

                    @Override
                    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
                        TesterThread.runTests();
                        src.sendMessage(Text.of(TextColors.GREEN, "Here we go!"));

                        return CommandResult.success();
                    }
                }).build(),
                "runTest");
    }

    public static Player getThePlayer() {
        Collection<Player> players = Sponge.getServer().getOnlinePlayers();
        if (players.size() != 1) {
            throw new RuntimeException("Unexpected players: " + players);
        }
        return players.iterator().next();
    }

    public void sendToPlayer(MessageRPCRequest messageRPCRequest) {
        this.channel.sendTo(McTester.getThePlayer(), messageRPCRequest);
    }
}
