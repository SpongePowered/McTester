package pw.aaron1011.mctester.testcase;

import com.google.common.base.Preconditions;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;
import pw.aaron1011.mctester.TestUtils;
import pw.aaron1011.mctester.framework.Client;

public class ChatTest {

    public void runTest(Game game, Client client) {

        final Text[] recievedMessage = new Text[1];

        TestUtils.listenOneShot(MessageChannelEvent.Chat.class, new EventListener<MessageChannelEvent.Chat>() {

            @Override
            public void handle(MessageChannelEvent.Chat event) throws Exception {
                recievedMessage[0] = event.getRawMessage();
            }
        });

        client.sendMessage("Hello, world!");

        Preconditions.checkArgument(recievedMessage[0].equals(Text.of("Hello, world!")));
    }

}
