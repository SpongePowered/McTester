package org.spongepowered.mctester.internal.framework;

import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.mctester.internal.RawClient;
import org.spongepowered.mctester.internal.framework.proxy.ResponseCallback;
import org.spongepowered.mctester.internal.interfaces.IMixinMinecraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class RealClientHandler implements RawClient {

    private long clientTick = 0L;
    private Map<Long, ResponseCallback> endTicks = new HashMap<>();

    @Override
    public void sendMessage(String text) {
        Minecraft minecraft = Minecraft.getMinecraft();
        minecraft.displayGuiScreen(new GuiChat());

        GuiChat chat = (GuiChat) minecraft.currentScreen;
        chat.sendChatMessage(text);
        minecraft.displayGuiScreen(null);
    }

    @Override
    public void lookAtBlock(Vector3i targetBlock) {
        // Look at the center of the block
        this.lookAt(targetBlock.toDouble().add(0.5, 1, 0.5));
    }

    @Override
    public void lookAt(Vector3d targetPos) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;

        // The code in SpongeAPI Living#lookAt is horrendously broken - it sets one or both of the player
        // rotation values to NaN in several cases.

        // the following code is adapted http://wiki.vg/Protocol

        double dx = targetPos.getX() - player.posX;
        double dy = targetPos.getY() - (player.posY + (double) player.getEyeHeight());
        double dz = targetPos.getZ() - player.posZ;
        double r = (double) MathHelper.sqrt((dx * dx) + (dy * dy) + (dz * dz));

        double yaw = -Math.atan2(dx, dz)*(180/Math.PI);
        if (yaw < 0) {
            yaw = 360 + yaw;
        }
        double pitch = -Math.asin(dy/r)*(180/Math.PI);

        player.rotationYaw = (float) yaw;
        player.rotationYawHead = (float) yaw;
        player.rotationPitch = (float) pitch;
    }

    private float updateRotation(float p_75652_1_, float p_75652_2_)
    {
        float f = MathHelper.wrapDegrees(p_75652_2_ - p_75652_1_);

        return p_75652_1_ + f;
    }

    @Override
    public void lookAt(UUID entityUUID) {
        // Copied from EntityLookHelper
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        net.minecraft.entity.Entity entity = getEntity(entityUUID).get();

        Vector3d target = new Vector3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
        this.lookAt(target);

        /*double d0 = entity.posX - player.posX;
        double d1 = entity.posY - (player.posY + (double)player.getEyeHeight());
        double d2 = entity.posZ - player.posZ;
        double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
        float f = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
        float f1 = (float)(-(MathHelper.atan2(d1, d3) * (180D / Math.PI)));
        player.rotationPitch = f1;
        player.rotationYawHead = f;*/
    }

    // Copied from MixinWorld
    public Optional<net.minecraft.entity.Entity> getEntity(UUID uuid) {
        // Note that MixinWorldServer is properly overriding this to use it's own mapping.
        for (net.minecraft.entity.Entity entity : Minecraft.getMinecraft().world.loadedEntityList) {
            if (entity.getUniqueID().equals(uuid)) {
                return Optional.of(entity);
            }
        }
        return Optional.empty();
    }

    @Override
    public void selectHotbarSlot(int slot) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;

        // Copied from Minecraft#processKeyBinds, with some modifications

        if (Minecraft.getMinecraft().player.isSpectator())
        {
            Minecraft.getMinecraft().ingameGUI.getSpectatorGui().onHotbarSelected(slot);
        }
        else
        {
            Minecraft.getMinecraft().player.inventory.currentItem = slot;
        }
    }

    @Override
    public void leftClick() {
        ((IMixinMinecraft) Minecraft.getMinecraft()).leftClick();
    }

    @Override
    public void holdLeftClick(boolean clicking) {
        ((IMixinMinecraft) Minecraft.getMinecraft()).holdLeftClick(clicking);
    }


    @Override
    public void rightClick() {
        ((IMixinMinecraft) Minecraft.getMinecraft()).rightClick();
    }

    @Override
    public void holdRightClick(boolean clicking) {
        ((IMixinMinecraft) Minecraft.getMinecraft()).holdRightClick(clicking);
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

    @Override
    public String getOpenGuiClass() {
        if (Minecraft.getMinecraft().currentScreen == null) {
            return "";
        }
        return Minecraft.getMinecraft().currentScreen.getClass().getName();
    }

    @Override
    public void onFullyLoggedIn() {
    }

    @Override
    public void sleepTicksClient(int ticks, ResponseCallback callback) {
        this.endTicks.put(this.clientTick + ticks, callback);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        this.clientTick++;
        Iterator<Map.Entry<Long, ResponseCallback>> it = this.endTicks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, ResponseCallback> entry = it.next();
            if (entry.getKey() == this.clientTick) {
                entry.getValue().sendResponse(null);
                it.remove();
            }
        }
    }
}
