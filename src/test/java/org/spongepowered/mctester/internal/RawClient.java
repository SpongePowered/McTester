package org.spongepowered.mctester.internal;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;

import java.util.List;

public interface RawClient {

    void sendMessage(String text);

    void lookAt(Vector3d targetPos);

    void selectHotbarSlot(int slot);

    void leftClick();

    void rightClick();

    ItemStack getItemInHand(HandType type);

    List<DataView> getRawInventory();

}