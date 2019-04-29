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
package org.spongepowered.mctester.testmixins;

import net.minecraft.entity.passive.EntitySheep;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.mctester.CustomMixinsTest;
import org.spongepowered.mctester.IMixinEntitySheep;

import java.util.Optional;
import java.util.UUID;

@Mixin(value = EntitySheep.class, remap = false)
public class MixinEntitySheep_CustomMixins implements IMixinEntitySheep {

    private UUID shearedBy;

    @Inject(method = "onSheared", at = @At(value = "HEAD"))
    private void onShear(CallbackInfoReturnable<Boolean> ci) {
        if (CustomMixinsTest.testRunning.isRunning()) {
            this.shearedBy = Sponge.getCauseStackManager().getCurrentCause().first(Player.class).get().getUniqueId();
        }
    }

    @Override
    public Optional<UUID> getShearedBy() {
        return Optional.ofNullable(this.shearedBy);
    }
}
