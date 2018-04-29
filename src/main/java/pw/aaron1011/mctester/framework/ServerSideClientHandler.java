package pw.aaron1011.mctester.framework;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.item.inventory.ItemStack;
import pw.aaron1011.mctester.McTester;
import pw.aaron1011.mctester.TestUtils;
import pw.aaron1011.mctester.message.toclient.MessageChat;

public class ServerSideClientHandler implements Client {

    @Override
    public void sendMessage(String text) {
        //McTester.INSTANCE.handler.addOutbound(new MessageChat(text));
        //this.waitForAck();
    }

    @Override
    public void lookAt(Vector3d targetPos) {

    }

    @Override
    public void selectHotbarSlot(int slot) {

    }

    @Override
    public void leftClick() {

    }

    @Override
    public void rightClick() {

    }

    @Override
    public ItemStack getItemInHand(HandType type) {
        return null;
    }

    private void waitForAck() {
        //TestUtils.waitForSignal();
    }
}
