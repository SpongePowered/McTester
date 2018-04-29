package pw.aaron1011.mctester.framework.proxy;

import org.spongepowered.api.scheduler.SpongeExecutorService;
import pw.aaron1011.mctester.McTester;
import pw.aaron1011.mctester.ServerOnly;
import pw.aaron1011.mctester.framework.Client;
import pw.aaron1011.mctester.message.ResponseWrapper;
import pw.aaron1011.mctester.message.toclient.MessageRPCRequest;

import java.util.concurrent.TimeUnit;

public class RemoteClientProxy extends BaseProxy {

    public RemoteClientProxy(SpongeExecutorService mainThreadExecutor) {
        super(null, mainThreadExecutor);
    }

    public static Client newProxy() {
        RemoteClientProxy proxy = new RemoteClientProxy(McTester.INSTANCE.syncExecutor);
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
