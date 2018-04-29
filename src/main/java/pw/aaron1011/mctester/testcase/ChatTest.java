package pw.aaron1011.mctester.testcase;

import com.google.common.base.Preconditions;
import org.spongepowered.api.Game;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackComparators;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import pw.aaron1011.mctester.McTester;
import pw.aaron1011.mctester.TestUtils;
import pw.aaron1011.mctester.framework.Client;

public class ChatTest {

    public void runTest(Game game, Client client, TestUtils testUtils) {

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

        ItemStack clientStack = client.getItemInHand(HandTypes.MAIN_HAND);
        boolean eq = ItemStackComparators.ALL.compare(serverStack, clientStack) == 0;
        game.getServer().getBroadcastChannel().send(Text.of("Stacks are equal: " + eq), ChatTypes.SYSTEM);

    }

}
