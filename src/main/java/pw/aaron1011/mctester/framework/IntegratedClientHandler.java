package pw.aaron1011.mctester.framework;

import pw.aaron1011.mctester.McTester;
import pw.aaron1011.mctester.TestUtils;
import pw.aaron1011.mctester.message.toclient.MessageChat;

public class IntegratedClientHandler implements Client {

    @Override
    public void sendMessage(String text) {
        McTester.INSTANCE.handler.addOutbound(new MessageChat(text));
        this.waitForAck();
    }

    private void waitForAck() {
        TestUtils.waitForSignal();
    }
}
