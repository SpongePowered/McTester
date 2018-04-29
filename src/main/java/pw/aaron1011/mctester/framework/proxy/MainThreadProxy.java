package pw.aaron1011.mctester.framework.proxy;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import pw.aaron1011.mctester.McTester;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainThreadProxy extends BaseProxy {

    public MainThreadProxy(Object realObject, SpongeExecutorService mainThreadExecutor) {
        super(realObject, mainThreadExecutor);
    }

    public static boolean shouldProxy(Object object) {
        return object.getClass().getName().startsWith("org.spongepowered") && !(object.getClass().getName().equals(BaseProxy.class.getName()));
    }

    public static <T> T newProxy(T object) {
        if (shouldProxy(object)) {
            MainThreadProxy proxy = new MainThreadProxy(object, McTester.INSTANCE.syncExecutor);
            return (T) proxy.makeProxy(object.getClass());
        }
        return object;
    }

    @Override
    Object dispatch(InvocationData data) {

        SpongeExecutorService.SpongeFuture<Object> future = this.mainThreadExecutor.schedule(data, 0, TimeUnit.SECONDS);

        // Blocking
        Object result;
        try {
            result = future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Object proxied = MainThreadProxy.newProxy(result);
        return proxied;
    }
}
