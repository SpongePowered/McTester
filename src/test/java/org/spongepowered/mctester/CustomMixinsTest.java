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
package org.spongepowered.mctester;

import com.flowpowered.math.vector.Vector3d;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.animal.Sheep;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.mctester.api.TestRunning;
import org.spongepowered.mctester.api.junit.MinecraftRunner;
import org.spongepowered.mctester.internal.BaseTest;
import org.spongepowered.mctester.internal.event.StandaloneEventListener;
import org.spongepowered.mctester.junit.TestUtils;

import java.util.Optional;
import java.util.UUID;

@RunWith(MinecraftRunner.class)
public class CustomMixinsTest extends BaseTest {

    public CustomMixinsTest(TestUtils testUtils) {
        super(testUtils);
    }

    @ClassRule public static TestRunning testRunning = new TestRunning();

    @Test
    public void testShearSheep() throws Throwable {
        UUID playerUUID = this.testUtils.runOnMainThread(() -> {
            Player player = this.testUtils.getThePlayer();
            player.offer(Keys.GAME_MODE, GameModes.CREATIVE);
            return player.getUniqueId();
        });

        this.testUtils.listen(new StandaloneEventListener<>(MoveEntityEvent.class, (event) -> {
            if (event.getTargetEntity() instanceof Sheep) {
                event.setCancelled(true);
            }
        }));

        testUtils.waitForInventoryPropagation();

        // Look at the ground two blocks in the z direction
        Vector3d targetPos = this.testUtils.runOnMainThread(() -> CustomMixinsTest.this.testUtils.getThePlayer().getLocation().getPosition().add(0, -1, 2));
        client.lookAt(targetPos);

        Entity entitySheep = this.testUtils.runOnMainThread(() -> {
            Entity sheep = this.testUtils.getThePlayer().getWorld().createEntity(EntityTypes.SHEEP, targetPos.add(0, 1, -1));
            this.testUtils.getThePlayer().getWorld().spawnEntity(sheep);

            Player player = this.testUtils.getThePlayer();

            ((Hotbar) player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class))).setSelectedSlotIndex(0);
            player.setItemInHand(
                    HandTypes.MAIN_HAND, ItemStack.of(ItemTypes.SHEARS));

            return sheep;
        });

        testUtils.waitForAll();

        this.client.lookAt(entitySheep);
        client.rightClick();

        Optional<UUID> shearedBy = ((IMixinEntitySheep) entitySheep).getShearedBy();
        Assert.assertEquals(Optional.of(playerUUID), shearedBy);
    }
}
