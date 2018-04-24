package pw.aaron1011.mctester.message.toclient;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

public class MessageChat implements Message {

    public String message;

    public MessageChat(String message) {
        this.message = message;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.message = buf.readString();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeString(this.message);
    }
}
