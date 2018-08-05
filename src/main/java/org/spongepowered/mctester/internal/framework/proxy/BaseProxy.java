package org.spongepowered.mctester.internal.framework.proxy;

import org.apache.commons.lang3.ClassUtils;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.mctester.internal.McTester;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

public abstract class BaseProxy implements InvocationHandler {

    public Object realObject;
    SpongeExecutorService mainThreadExecutor;
    private String className;
    protected ProxyCallback callback;

    public BaseProxy(Object realObject, SpongeExecutorService mainThreadExecutor, ProxyCallback callback) {
        this.realObject = realObject;
        this.mainThreadExecutor = mainThreadExecutor;
        this.className = this.getClass().getSimpleName();
        this.callback = callback;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        if (method.getName().equals("toString") && method.getDeclaringClass().equals(Object.class)) {
            return this.className + "(" + this.realObject + ")";
        }

        if (objects == null) {
            objects = new Object[0];
        }
        this.unwrapProxied(objects);

        InvocationData data = new InvocationData(this.realObject, method, Arrays.asList(objects));
        Object result = this.dispatch(data);

        if (this.callback != null) {
            this.callback.afterInvoke();
        }

        return result;
    }

    /**
     * If we're invoking a proxied method, we've already taken
     * care of setting up the proper environemt (e.g running on the main thread)
     * This allows us to unwrap any proxied objects that may have been passed as arguments.
     * @param args
     */
    private void unwrapProxied(Object[] args) {
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                continue;
            }
            if (Proxy.isProxyClass(arg.getClass()) && Proxy.getInvocationHandler(arg) instanceof BaseProxy) {
                args[i] = ((BaseProxy) Proxy.getInvocationHandler(arg)).realObject;
            }
        }
    }

    <T> T makeProxy(Class<T> type, Class<?>[] interfaces) {
        return (T) Proxy.newProxyInstance(McTester.class.getClassLoader(), interfaces, this);
    }


    protected static Class<?>[] getAllInterfaces(Class<?> clazz) {
        List<Class<?>> interfaces = ClassUtils.getAllInterfaces(clazz);
        if (clazz.isInterface()) {
            interfaces.add(clazz);
        }
        return interfaces.stream().filter(i -> !i.getName().startsWith("net.minecraft")).toArray(Class[]::new);
        /*List<Class<?>> interfaces = new ArrayList<>();
        while (!clazz.equals(Object.class)) {
            interfaces.addAll(Arrays.asList(clazz.getInterfaces()));
            clazz = clazz.getSuperclass();
        }

        Class<?>[] interfacesFinal = new Class[interfaces.size()];
        interfaces.toArray(interfacesFinal);

        return interfacesFinal;*/
    }

    abstract Object dispatch(InvocationData data);
}
