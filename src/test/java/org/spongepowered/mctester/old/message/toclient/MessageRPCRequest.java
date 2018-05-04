package org.spongepowered.mctester.old.message.toclient;

import org.spongepowered.mctester.old.framework.proxy.RemoteInvocationData;
import net.minecraft.client.Minecraft;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.mctester.old.McTester;

public class MessageRPCRequest extends BaseClientMessage {

    private RemoteInvocationData data;

    public MessageRPCRequest() {}

    public MessageRPCRequest(RemoteInvocationData data) {
        this.data = data;
    }

    @Override
    public void process() {
        Minecraft.getMinecraft().addScheduledTask(new Runnable() {

            @Override
            public void run() {
                try {
                    McTester.INSTANCE.channel.sendToServer(MessageRPCRequest.this.data.makeResponse());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.data = Sponge.getDataManager().getBuilder(RemoteInvocationData.class).get().build(buf.readDataView()).get();

        /*DataContainer container = DataContainer.createNew();
        container.set()
        buf.writeString(this.data.method.getName());
        Class<?>[] types = this.data.method.getParameterTypes();

        // JVM methods can have at most 255 parameters
        buf.writeByte((byte) types.length);
        for (Class<?> type: types) {
            buf.writeString(type.getCanonicalName());
        }*/
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeDataView(this.data.toContainer());
    }
}
