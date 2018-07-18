package org.spongepowered.mctester.internal.framework.proxy;

import org.apache.commons.lang3.ClassUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.mctester.internal.McTester;

import java.util.concurrent.TimeUnit;

public class MainThreadProxy extends BaseProxy {

    public MainThreadProxy(Object realObject, SpongeExecutorService mainThreadExecutor, ProxyCallback callback) {
        super(realObject, mainThreadExecutor, callback);
    }

    public static boolean shouldProxy(Object object, Class<?> realType, Class<?>[] interfaces) {
        return /*object != null
                && (object.getClass().getName().startsWith("org.spongepowered") || object.getClass().getName().startsWith("net.minecraft")) // Proxy anything from Sponge or Minecraft...
                &&*/ !(object.getClass().getName().equals(BaseProxy.class.getName())) // unless it's another proxy, because that would be dumb
                    && !ClassUtils.isPrimitiveOrWrapper(object.getClass())
                    && interfaces.length > 0
                    && (realType.isInterface() || realType.equals(Object.class));
                //&& !Modifier.isFinal(object.getClass().getModifiers());
    }

    public static <T> T newProxy(T object, Class<?> realType, ProxyCallback callback) {
        if (object == null) {
            return null;
        }

        Class<?>[] interfaces = BaseProxy.getAllInterfaces(object.getClass());
        boolean doProxy = shouldProxy(object, realType, interfaces);
        if (doProxy) {
            MainThreadProxy proxy = new MainThreadProxy(object, McTester.INSTANCE.syncExecutor, callback);
            return (T) proxy.makeProxy(object.getClass(), interfaces);
        }
        return object;
    }

    @Override
    Object dispatch(InvocationData data) {

        Object result;
        // If we're already on the main thread, don't bother using the scheduler
        if (Sponge.getServer().isMainThread()) {
            try {
                result = data.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            SpongeExecutorService.SpongeFuture<Object> future = this.mainThreadExecutor.schedule(data, 0, TimeUnit.SECONDS);

            // Blocking
            try {
                result = future.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        Object proxied = MainThreadProxy.newProxy(result, data.method.getReturnType(), this.callback);
        return proxied;
    }
}
