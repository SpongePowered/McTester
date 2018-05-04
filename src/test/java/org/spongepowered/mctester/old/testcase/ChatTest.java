package org.spongepowered.mctester.old.testcase;

public class ChatTest {

    /*public void runTest(Game game, Client client, TestUtils testUtils) {

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

    }*/

}
