package org.spongepowered.mctester.internal.framework;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.noise.module.combiner.Min;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumHand;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.mctester.internal.RawClient;

import java.util.ArrayList;
import java.util.List;

public class RealClientHandler implements RawClient {

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

    @Override
    public List<DataView> getRawInventory() {
        InventoryPlayer inventory = Minecraft.getMinecraft().player.inventory;
        int size = inventory.getSizeInventory();
        List<DataView> stacks = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            stacks.add(((ItemStack) (Object) inventory.getStackInSlot(i)).toContainer());
        }
        return stacks;
    }
}
