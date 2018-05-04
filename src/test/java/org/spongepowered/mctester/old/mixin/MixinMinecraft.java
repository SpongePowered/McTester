package org.spongepowered.mctester.old.mixin;

import org.spongepowered.mctester.old.interfaces.IMixinMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft implements IMixinMinecraft {

    @Shadow public abstract void displayGuiScreen(@Nullable GuiScreen guiScreenIn);

    @Shadow public abstract void launchIntegratedServer(String folderName, String worldName, @Nullable WorldSettings worldSettingsIn);

    @Shadow private volatile boolean running;

    @Redirect(method = "init", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;serverName:Ljava/lang/String;", ordinal = 0))
    public String onGetServerName(Minecraft minecraft) {
        return "blah";
    }

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/client/FMLClientHandler;connectToServerAtStartup(Ljava/lang/String;I)V"))
    public void onConnect(FMLClientHandler handler, String serverName, int serverPort) {
        this.displayGuiScreen(null);

        long seed = new Random().nextLong();
        String folderName = "MCTestWorld-" + String.valueOf(seed).substring(0, 5);

        WorldSettings worldsettings = new WorldSettings(seed, GameType.CREATIVE, false, false, WorldType.FLAT);
        this.launchIntegratedServer(folderName, folderName, worldsettings);
    }

    @Redirect(method = "shutdownMinecraftApplet", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/asm/transformers/TerminalTransformer$ExitVisitor;systemExitCalled(I)V"))
    public void onSystemExitCalled(int code) {
        // Do nothing - we want to exit cleanly through JUnit
    }

    @Inject(method = "stopIntegratedServer", at = @At("HEAD"), cancellable = true)
    private static void onStopIntegratedServer(CallbackInfo ci) {
        // If we're already shutting down, don't try to shutdown again
        if (Minecraft.getMinecraft() != null && !((IMixinMinecraft) Minecraft.getMinecraft()).isRunning()) {
            ci.cancel();
        }
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

}
