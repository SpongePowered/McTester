package org.spongepowered.mctester.internal.framework.proxy;

import org.spongepowered.mctester.internal.McTester;
import org.spongepowered.mctester.internal.message.toserver.MessageRPCResponse;

public class ResponseCallback {

    private Class<?> returnType;

    public ResponseCallback(Class<?> returnType) {
        this.returnType = returnType;
    }

    public void sendResponse(Object returnValue) {
        MessageRPCResponse response = new MessageRPCResponse(returnValue, this.returnType);
        McTester.INSTANCE.channel.sendToServer(response);
    }
}
