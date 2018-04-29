package pw.aaron1011.mctester.message.toserver;

import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.network.ChannelBuf;
import pw.aaron1011.mctester.message.toclient.BaseClientMessage;

public class MessageItemStack extends BaseServerMessage {

    private ItemStack itemStack;

    public MessageItemStack() {}

    public MessageItemStack(ItemStack stack) {
        this.itemStack = stack;
    }

    @Override
    public void process() {

    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.itemStack = ItemStack.builder().fromContainer(buf.readDataView()).build();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeDataView(this.itemStack.toContainer());
    }
}
