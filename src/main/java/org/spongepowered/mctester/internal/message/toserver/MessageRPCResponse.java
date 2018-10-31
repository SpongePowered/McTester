package org.spongepowered.mctester.internal.message.toserver;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.mctester.internal.ServerOnly;
import org.spongepowered.mctester.internal.Utils;
import org.spongepowered.mctester.internal.message.RPCKeys;
import org.spongepowered.mctester.internal.message.ResponseWrapper;

public class MessageRPCResponse extends BaseServerMessage {

    private Object response;
    private Class<?> type;

    public MessageRPCResponse() {}

    public MessageRPCResponse(Object response, Class<?> type) {
        this.response = response;
        this.type = type;
    }

    @Override
    public void process() {
        // We deliberately avoid using Sponge's scheduler API here.
        // All Sponge synchronous tasks run at the start of the tick (TickEvent.Phase.START)
        // which is *before* any packet scheduled tasks.

        // If an ACK packet arrives after another packet, but during the same tick,
        // we need to ensure that their respective handlers run on the main thread
        // in the proper order. If we were to use the Sponge scheduler API, our ACK
        // packet handler would run *before* the other packet handler, even though
        // it arrived *after*

        // The reason we use a scheduler at all is to ensure that all preceding packets
        // have been processed.
        ((MinecraftServer) Sponge.getServer()).addScheduledTask(
                () -> ServerOnly.INSTANCE.addResonseBlocking(new ResponseWrapper(MessageRPCResponse.this.response)));
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.type = Utils.classForName(buf.readString());

        if (this.type != void.class) {
            DataView view = buf.readDataView();
            this.response = view.getView(RPCKeys.RESPONSE.getQuery()).flatMap(v -> Utils.dataToArbitrary(v, this.type))
                    .orElseGet(() -> view.get(RPCKeys.RESPONSE.getQuery()).get());
        }
        //this.response = Utils.dataToArbitrary(view.getView(RPCKeys.RESPONSE.getQuery()).get(), this.type).orElse(view.get(RPCKeys.));
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeString(this.type.getCanonicalName());
        if (this.type != void.class) {
            buf.writeDataView(DataContainer.createNew().set(RPCKeys.RESPONSE, this.response));
        }
    }
}
