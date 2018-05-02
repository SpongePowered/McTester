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
package org.spongepowered.mctester.test;

import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackComparators;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.mctester.main.McTester;
import org.spongepowered.mctester.main.TestUtils;
import org.spongepowered.mctester.main.framework.Client;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spongepowered.api.Game;
import org.spongepowered.mctester.test.internal.MinecraftRunner;

/**
 * Integration test for CommandTestHelper.
 *
 * @author Michael Vorburger
 */
@RunWith(MinecraftRunner.class)
public class RealChatTest {

	private Game game;
	private Client client;
	private TestUtils testUtils;

	public RealChatTest(Game game, Client client, TestUtils testUtils) {
		this.game = game;
		this.client = client;
		this.testUtils = testUtils;

	}


	@Test
	public void helpHelp() {
		final Text[] recievedMessage = new Text[1];

		testUtils.listenOneShot(MessageChannelEvent.Chat.class, new EventListener<MessageChannelEvent.Chat>() {

			@Override
			public void handle(MessageChannelEvent.Chat event) throws Exception {
				recievedMessage[0] = event.getRawMessage();
			}
		});
		client.sendMessage("Hello, world!");

		game.getServer().getBroadcastChannel().send(Text.of("From a different thread!"), ChatTypes.SYSTEM);
		game.getServer().getBroadcastChannel().send(Text.of("Success: ", recievedMessage[0]), ChatTypes.SYSTEM);

		ItemStack serverStack = ItemStack.of(ItemTypes.GOLD_INGOT, 5);

		Hotbar hotbar = (Hotbar) McTester.getThePlayer().getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class));
		hotbar.set(new SlotIndex(hotbar.getSelectedSlotIndex()), serverStack);

		// We sleep two ticks to guarantee that the client has been updated.
		// During the next tick, the server will send our inventory changes to the client.
		// However, we don't want to rely on this happening at any particular point during the tick,
		// so we wait two ticks to guarantee that the update packets have been sent by the time
		// our code runs.
		testUtils.sleepTicks(2);

		ItemStack clientStack = client.getItemInHand(HandTypes.MAIN_HAND);
		boolean eq = ItemStackComparators.ALL.compare(serverStack, clientStack) == 0;
		game.getServer().getBroadcastChannel().send(Text.of("Stacks: " + serverStack + " " + clientStack + " " + eq), ChatTypes.SYSTEM);
	}


}
