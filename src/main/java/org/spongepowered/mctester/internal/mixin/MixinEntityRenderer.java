package org.spongepowered.mctester.internal.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = EntityRenderer.class, remap = false)
public abstract class MixinEntityRenderer {

    /**
     * Prevents the mouse from messing with the current test. Also stops
     * Minecraft from moving the mouse on its own on macOS.
     */
    @Redirect(method = "updateCameraAndRender", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;inGameHasFocus:Z",
            opcode = Opcodes.GETFIELD))
    private boolean shouldUseMouse(Minecraft mc) {
        return false;
    }
}
