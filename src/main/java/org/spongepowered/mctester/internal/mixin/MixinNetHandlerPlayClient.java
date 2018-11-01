/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.mctester.internal.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.mctester.internal.interfaces.IMixinMinecraft;

@Mixin(value = NetHandlerPlayClient.class, remap = false)
public abstract class MixinNetHandlerPlayClient {

    @Shadow public abstract void cleanup();

    @Inject(method = "onDisconnect", at = @At(value = "RETURN"))
    public void onOnDisconnectInvoked(CallbackInfo ci) {
        // We move the call to cleanup() from Minecraft.loadWorld(null) to
        // this method. Minecraft.loadWorld(null) is invoked from places
        // onther than the onDisconnect handler (e.g. GuiGameOver), where it's
        // not safe to call cleanup(). When we call onCleanup(), we need to be certain
        // that we'll never process any more packets from the server, or we may get
        // an NPE if a packet handler tries to access 'world'

        // Therefore, we add a client scheduled task which calls cleanup.
        // At this point, it's impossible for any more packets handlers
        // to be added to the scheduled task queue - the Netty channel is closed,
        // so NetworkManager#channelRead0 will never call processPacket again (for our
        // current server)

        // If there *are* packet handlers currently in the queue,
        // our scheduled task will be added after all of them. Since no more
        // packet handlers will be added, we're guaranteed to call cleanup()
        // only after all packets from the server have been processed.

        //
        // By calling cleanup() here, we're guaranteed that we'll never
        // process any further packets from the server (since onDisconnect
        ((IMixinMinecraft) Minecraft.getMinecraft()).addScheduledTaskAlwaysDelay(() -> {
            this.cleanup();
            return null;
        });

    }

}
