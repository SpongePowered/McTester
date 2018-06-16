package org.spongepowered.mctester.internal;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RawClient {

    void sendMessage(String text);

    void lookAt(Vector3d targetPos);

    void lookAt(UUID entity);

    void selectHotbarSlot(int slot);

    void leftClick();

    void holdLeftClick(boolean clicking);

    void rightClick();

    void holdRightClick(boolean clicking);

    void onFullyLoggedIn();

    ItemStack getItemInHand(HandType type);

    List<DataView> getRawInventory();

    String getOpenGuiClass();
}
