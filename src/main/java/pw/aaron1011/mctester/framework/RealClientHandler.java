package pw.aaron1011.mctester.framework;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.util.EnumHand;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import pw.aaron1011.mctester.message.toclient.MessageChat;

public class RealClientHandler implements Client {

    @Override
    public void sendMessage(String text) {
        Minecraft minecraft = Minecraft.getMinecraft();
        minecraft.displayGuiScreen(new GuiChat());

        GuiChat chat = (GuiChat) minecraft.currentScreen;
        chat.sendChatMessage(text);
        minecraft.displayGuiScreen(null);
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
        return (ItemStack) (Object) Minecraft.getMinecraft().player.getHeldItem((EnumHand) (Object) type);
    }
}
