package pw.aaron1011.mctester.message.toserver;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;
import pw.aaron1011.mctester.McTester;

public class MessageAck extends BaseServerMessage {

    @Override
    public void readFrom(ChannelBuf buf) {

    }

    @Override
    public void writeTo(ChannelBuf buf) {

    }

    @Override
    public void process() {
        //McTester.INSTANCE.handler.receiveAck(this);
    }
}
