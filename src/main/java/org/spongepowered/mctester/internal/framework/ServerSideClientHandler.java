package org.spongepowered.mctester.internal.framework;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.mctester.internal.McTester;
import org.spongepowered.mctester.internal.RawClient;

import java.util.List;

public class ServerSideClientHandler implements Client {

    private RawClient proxyClient;

    public ServerSideClientHandler(RawClient proxyClient) {
        this.proxyClient = proxyClient;
    }


    @Override
    public PlayerInventory getClientInventory() {
        List<DataView> itemStacks = this.proxyClient.getRawInventory();
        InventoryPlayer inventory = new InventoryPlayer((EntityPlayer) McTester.getThePlayer());

        for (int i = 0; i < itemStacks.size(); i++) {
            inventory.setInventorySlotContents(i, (net.minecraft.item.ItemStack) (Object) ItemStack.builder().fromContainer(itemStacks.get(i)).build());
        }
        return (PlayerInventory) inventory;
    }


    // Boilerplate

    @Override
    public void sendMessage(String text) {
        this.proxyClient.sendMessage(text);
    }

    @Override
    public void lookAt(Vector3d targetPos) {
        this.proxyClient.lookAt(targetPos);
    }

    @Override
    public void lookAt(Entity entity) {
        this.proxyClient.lookAt(entity.getUniqueId());
    }

    @Override
    public void selectHotbarSlot(int slot) {
        this.proxyClient.selectHotbarSlot(slot);
    }

    @Override
    public void leftClick() {
        this.proxyClient.leftClick();
    }

    @Override
    public void holdLeftClick(boolean clicking) {
        this.proxyClient.holdLeftClick(clicking);
    }

    @Override
    public void rightClick() {
        this.proxyClient.rightClick();
    }

    @Override
    public void holdRightClick(boolean clicking) {
        this.proxyClient.holdRightClick(clicking);
    }

    @Override
    public ItemStack getItemInHand(HandType type) {
        return this.proxyClient.getItemInHand(type);
    }
}
