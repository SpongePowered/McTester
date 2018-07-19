package org.spongepowered.mctester.internal.framework;

import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector3d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumHand;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.mctester.internal.RawClient;
import org.spongepowered.mctester.internal.interfaces.IMixinMinecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        EntityPlayerSP player = Minecraft.getMinecraft().player;

        // Copied from SpongeAPI Living
        Vector3d eyePos = new Vector3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);

        Vector2d xz1 = eyePos.toVector2(true);
        Vector2d xz2 = targetPos.toVector2(true);
        double distance = xz1.distance(xz2);

        if (distance == 0) {
            return;
        }

        // calculate pitch
        Vector2d p1 = Vector2d.UNIT_Y.mul(eyePos.getY());
        Vector2d p2 = new Vector2d(distance, targetPos.getY());
        Vector2d v1 = p2.sub(p1);
        Vector2d v2 = Vector2d.UNIT_X.mul(distance);
        final double pitchRad = Math.acos(v1.dot(v2) / (v1.length() * v2.length()));
        final double pitchDeg = pitchRad * 180 / Math.PI * (-v1.getY() / Math.abs(v1.getY()));

        // calculate yaw
        p1 = xz1;
        p2 = xz2;
        v1 = p2.sub(p1);
        v2 = Vector2d.UNIT_Y.mul(v1.getY());
        double yawRad = Math.acos(v1.dot(v2) / (v1.length() * v2.length()));
        double yawDeg = yawRad * 180 / Math.PI;
        if (v1.getX() < 0 && v1.getY() < 0) {
            yawDeg = 180 - yawDeg;
        } else if (v1.getX() > 0 && v1.getY() < 0) {
            yawDeg = 270 - (90 - yawDeg);
        } else if (v1.getX() > 0 && v1.getY() > 0) {
            yawDeg = 270 + (90 - yawDeg);
        }

        player.rotationPitch = (float) pitchDeg;
        player.rotationYaw = (float) yawDeg;player.rotationYawHead = (float) yawDeg;
    }

    @Override
    public void lookAt(UUID entityUUID) {
        // Copied from EntityLookHelper
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        net.minecraft.entity.Entity entity = getEntity(entityUUID).get();
        System.err.println("Looking at: " + entity);

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
        System.err.println("Processed fully logged in!");
        //RunnerEvents.setPlayerJoined();
    }
}
