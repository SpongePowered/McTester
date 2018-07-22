package org.spongepowered.mctester.internal.mixin;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.mctester.internal.McTester;

@Mixin(NetworkManager.class)
public abstract class MixinNetworkManager {

    @Inject(method = "exceptionCaught", cancellable = true, at = @At(value = "NEW", target = "net/minecraft/util/text/TextComponentTranslation", ordinal = 0), remap = false)
    public void onTimeoutException(ChannelHandlerContext p_exceptionCaught_1_, Throwable p_exceptionCaught_2, CallbackInfo ci) {
        McTester.INSTANCE.logger.debug("Caught timeout exception in NetworkManager - printing out exception but keeping channel open!");
        LogManager.getLogger().debug(new TextComponentTranslation("disconnect.timeout").getUnformattedText(), p_exceptionCaught_2);
        ci.cancel();
    }

}
