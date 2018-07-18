package org.spongepowered.mctester.internal.mixin;

import jdk.internal.org.objectweb.asm.Opcodes;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NetHandlerPlayServer.class)
public abstract class MixinNetHandlerPlayServer {

    @Shadow private boolean field_194403_g;

    @Redirect(method = "processKeepAlive", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetHandlerPlayServer;disconnect(Lnet/minecraft/util/text/ITextComponent;)V"))
    private void onKeepAliveDisconnectTimeout(NetHandlerPlayServer this$0, ITextComponent reason) {
        this.field_194403_g = false;
        System.err.println("Cancelling timeout in NetHandlerPlayServer#processKeepAlive");
        // Do nothing - we don't want any timeouts during testing
    }

    @Redirect(method = "update", at = @At(value = "FIELD", target = "Lnet/minecraft/network/NetHandlerPlayServer;field_194403_g:Z", opcode = Opcodes.GETFIELD))
    private boolean onCheckUpdateDisconnectTimeout(NetHandlerPlayServer this$0) {
        System.err.println("Cancelling timeout in NetHandlerPlayServer#update");
        return false;
    }

}
