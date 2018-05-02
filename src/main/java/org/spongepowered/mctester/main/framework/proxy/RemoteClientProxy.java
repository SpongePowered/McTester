package org.spongepowered.mctester.main.framework.proxy;

import org.spongepowered.mctester.main.McTester;
import org.spongepowered.mctester.main.message.ResponseWrapper;
import org.spongepowered.mctester.main.message.toclient.MessageRPCRequest;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.mctester.main.ServerOnly;
import org.spongepowered.mctester.main.framework.Client;

import java.util.concurrent.TimeUnit;

public class RemoteClientProxy extends BaseProxy {

    public RemoteClientProxy(SpongeExecutorService mainThreadExecutor, ProxyCallback callback) {
        super(null, mainThreadExecutor, callback);
    }

    public static Client newProxy(ProxyCallback callback) {
        RemoteClientProxy proxy = new RemoteClientProxy(McTester.INSTANCE.syncExecutor, callback);
        return proxy.makeProxy(Client.class);
    }

    @Override
    Object dispatch(InvocationData data) {
        this.mainThreadExecutor.schedule(() -> McTester.INSTANCE.sendToPlayer(new MessageRPCRequest(new RemoteInvocationData(data))), 0, TimeUnit.SECONDS);

        try {
            // Blocking
            ResponseWrapper result = ServerOnly.INBOUND_QUEUE.take();
            return result.inner;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
