package org.spongepowered.mctester.internal.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mctester.internal.interfaces.IMixinMinecraftServer;

import javax.annotation.Nullable;

@Mixin(value = MinecraftServer.class, remap = false)
public class MixinMinecraftServer implements IMixinMinecraftServer {

    @Shadow private Thread serverThread;

    @Override
    public @Nullable Thread getServerMainThread() {
        return this.serverThread;
    }

}
