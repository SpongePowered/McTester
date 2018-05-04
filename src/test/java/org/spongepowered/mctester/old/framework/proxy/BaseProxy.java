package org.spongepowered.mctester.old.framework.proxy;

import org.apache.commons.lang3.ClassUtils;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.mctester.old.McTester;

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
        InvocationData data = new InvocationData(this.realObject, method, Arrays.asList(objects));
        Object result = this.dispatch(data);

        if (this.callback != null) {
            this.callback.afterInvoke();
        }

        return result;
    }

    <T> T makeProxy(Class<T> type) {
        return (T) Proxy.newProxyInstance(McTester.class.getClassLoader(), getAllInterfaces(type), this);
    }


    private static Class<?>[] getAllInterfaces(Class<?> clazz) {
        List<Class<?>> interfaces = ClassUtils.getAllInterfaces(clazz);
        if (clazz.isInterface()) {
            interfaces.add(clazz);
        }
        return interfaces.toArray(new Class[0]);
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
