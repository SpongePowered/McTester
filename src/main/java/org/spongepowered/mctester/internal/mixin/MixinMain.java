package org.spongepowered.mctester.internal.mixin;

import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Main.class)
public abstract class MixinMain {

    @Redirect(method = "main", at = @At(value = "INVOKE", target = "Ljava/lang/Runtime;addShutdownHook(Ljava/lang/Thread;)V"))
    private static void onAddShutdownHook(Runtime runtime, Thread thread) {
        // Do nothing - we register our own shutdown hook in MinecraftClientStarter
    }

}
