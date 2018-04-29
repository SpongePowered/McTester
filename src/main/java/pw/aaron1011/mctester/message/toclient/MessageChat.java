package pw.aaron1011.mctester.message.toclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import org.spongepowered.api.network.ChannelBuf;
import pw.aaron1011.mctester.McTester;
import pw.aaron1011.mctester.message.toserver.MessageAck;

public class MessageChat extends BaseClientMessage {

    public String message;

    public MessageChat() {}

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

    @Override
    public void process() {


        McTester.ack();
    }
}
