package org.spongepowered.mctester.internal;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.mctester.internal.framework.proxy.ResponseCallback;

import java.util.List;
import java.util.UUID;

public interface RawClient {

    void sendMessage(String text);

    void lookAtBlock(Vector3i targetBlock);

    void lookAt(Vector3d targetPos);

    void lookAt(UUID entity);

    void selectHotbarSlot(int slot);

    void leftClick();

    void holdLeftClick(boolean clicking);

    void rightClick();

    void holdRightClick(boolean clicking);

    void onFullyLoggedIn();

    void sleepTicksClient(int ticks, ResponseCallback callback);

    ItemStack getItemInHand(HandType type);

    List<DataView> getRawInventory();

    String getOpenGuiClass();
}
