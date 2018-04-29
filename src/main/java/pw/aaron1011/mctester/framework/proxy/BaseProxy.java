package pw.aaron1011.mctester.framework.proxy;

import static org.apache.commons.lang3.ArrayUtils.toArray;

import com.google.common.util.concurrent.SettableFuture;
import org.apache.commons.lang3.ClassUtils;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import pw.aaron1011.mctester.McTester;
import pw.aaron1011.mctester.TestUtils;
import pw.aaron1011.mctester.framework.TesterHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public abstract class BaseProxy implements InvocationHandler {

    public Object realObject;
    SpongeExecutorService mainThreadExecutor;
    private String className;

    public BaseProxy(Object realObject, SpongeExecutorService mainThreadExecutor) {
        this.realObject = realObject;
        this.mainThreadExecutor = mainThreadExecutor;
        this.className = this.getClass().getSimpleName();
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) {
        if (method.getName().equals("toString") && method.getDeclaringClass().equals(Object.class)) {
            return this.className + "(" + this.realObject + ")";
        }

        if (objects == null) {
            objects = new Object[0];
        }
        InvocationData data = new InvocationData(this.realObject, method, Arrays.asList(objects));
        Object result = this.dispatch(data);
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
