package org.spongepowered.mctester.internal.mixin;

import net.minecraft.client.main.Main;
import net.minecraft.launchwrapper.Launch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.mctester.api.RunnerEvents;

@Mixin(Main.class)
public abstract class MixinMain {

    @Inject(method = "main", at = @At("HEAD"), remap = false)
    private static void onMain(CallbackInfo ci) {
        RunnerEvents.setLaunchClassLoader(Launch.classLoader);
    }

    @Redirect(method = "main", at = @At(value = "INVOKE", target = "Ljava/lang/Runtime;addShutdownHook(Ljava/lang/Thread;)V"))
    private static void onAddShutdownHook(Runtime runtime, Thread thread) {
        // Do nothing - we register our own shutdown hook in MinecraftClientStarter
    }

}
