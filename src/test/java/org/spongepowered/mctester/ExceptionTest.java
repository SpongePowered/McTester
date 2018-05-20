package org.spongepowered.mctester;

import static org.junit.Assert.assertThat;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.mctester.internal.BaseTest;
import org.spongepowered.mctester.internal.TestUtils;
import org.spongepowered.mctester.internal.event.StandaloneEventListener;
import org.spongepowered.mctester.junit.MinecraftRunner;

@RunWith(MinecraftRunner.class)
public class ExceptionTest extends BaseTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    public ExceptionTest(TestUtils testUtils) {
        super(testUtils);
    }

    @Test
    public void testOneShotEventListenerException() throws Throwable {
        expectedEx.expect(AssertionError.class);
        expectedEx.expectMessage(CoreMatchers.containsString("Got message: Some message"));

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

    @Test
    public void testPermanentEventListenerException() throws Throwable {
        expectedEx.expect(AssertionError.class);
        expectedEx.expectMessage(CoreMatchers.containsString("Got message: Other message"));

        this.testUtils.listen(MessageChannelEvent.Chat.class,
                new StandaloneEventListener<MessageChannelEvent.Chat>() {

                    @Override
                    public Class<MessageChannelEvent.Chat> getEventClass() {
                        return MessageChannelEvent.Chat.class;
                    }

                    @Override
                    public void handle(MessageChannelEvent.Chat event) throws Exception {
                        Assert.fail("Got message: " + event.getRawMessage().toPlain());
                    }

                });

        this.testUtils.getClient().sendMessage("Other message");
    }

    @Test
    public void testTimeoutEventListenerException() throws Throwable {
        expectedEx.expect(AssertionError.class);
        expectedEx.expectMessage(CoreMatchers.containsString("Got message: From timeout"));

        this.testUtils.listenTimeout(() -> {
            this.testUtils.getClient().sendMessage("From timeout");
        }, new StandaloneEventListener<MessageChannelEvent.Chat>() {

            @Override
            public Class<MessageChannelEvent.Chat> getEventClass() {
                return MessageChannelEvent.Chat.class;
            }

            @Override
            public void handle(MessageChannelEvent.Chat event) throws Exception {
                Assert.fail("Got message: " + event.getRawMessage().toPlain());
            }
        }, 20);
    }

}
