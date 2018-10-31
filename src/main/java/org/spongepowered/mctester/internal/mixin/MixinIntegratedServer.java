package org.spongepowered.mctester.internal.mixin;

import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mctester.internal.interfaces.IMixinIntegratedServer;

@Mixin(value = IntegratedServer.class, remap = false)
public abstract class MixinIntegratedServer implements IMixinIntegratedServer {

    @Shadow private boolean isGamePaused;

    @Override
    public boolean isIntegratedServerPaused() {
        return this.isGamePaused;
    }
}
