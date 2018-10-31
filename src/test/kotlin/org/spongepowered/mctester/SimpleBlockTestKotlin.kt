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
package org.spongepowered.mctester

import org.hamcrest.core.IsEqual.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.spongepowered.api.block.BlockType
import org.spongepowered.api.block.BlockTypes
import org.spongepowered.api.data.type.HandTypes
import org.spongepowered.api.item.ItemTypes
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import org.spongepowered.mctester.api.WorldOptions
import org.spongepowered.mctester.api.junit.MinecraftRunner
import org.spongepowered.mctester.internal.BaseTest
import org.spongepowered.mctester.junit.CoroutineTest
import org.spongepowered.mctester.junit.TestUtils

@RunWith(MinecraftRunner::class)
@WorldOptions(deleteWorldOnSuccess = false)
class SimpleBlockTestKotlinI (testUtils: TestUtils) : BaseTest(testUtils) {

    @Test
    @Throws(Throwable::class)
    @CoroutineTest
    suspend fun testPlaceBlock() {
        val location = this.testUtils.thePlayer.location
        val target = location.add(0.0, -1.0, 1.0).getPosition()
        this.testUtils.client.lookAtSuspend(target)

        this.testUtils.thePlayer.setItemInHand(HandTypes.MAIN_HAND, ItemStack.of(ItemTypes.DIAMOND_BLOCK, 1))

        this.testUtils.client.rightClickSuspend()

        val type = this.testUtils.thePlayer.world.getBlockType(target.add(0f, 1f, 0f).toInt())

        assertThat<BlockType>(type, equalTo<BlockType>(BlockTypes.DIAMOND_BLOCK))
    }

}
