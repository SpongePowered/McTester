package org.spongepowered.mctester;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.mctester.internal.BaseTest;
import org.spongepowered.mctester.internal.TestUtils;
import org.spongepowered.mctester.internal.event.StandaloneEventListener;
import org.spongepowered.mctester.junit.MinecraftRunner;

@RunWith(MinecraftRunner.class)
public class ExceptionTest extends BaseTest {

    public ExceptionTest(TestUtils testUtils) {
        super(testUtils);
    }

    @Test(expected = AssertionError.class)
    public void testEventListenerException() throws Throwable {
        this.testUtils.listenOneShot(() -> {
            this.testUtils.getClient().sendMessage("Some message");
        }, new StandaloneEventListener<MessageChannelEvent.Chat>() {

            @Override
            public Class<MessageChannelEvent.Chat> getEventClass() {
                return MessageChannelEvent.Chat.class;
            }

            @Override
            public void handle(MessageChannelEvent.Chat event) throws Exception {
                Assert.fail("Got message: " + event.getRawMessage().toPlain());
            }
        });
    }

}
