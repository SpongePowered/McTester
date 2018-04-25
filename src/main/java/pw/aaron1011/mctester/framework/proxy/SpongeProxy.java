package pw.aaron1011.mctester.framework.proxy;

import com.google.common.util.concurrent.SettableFuture;
import pw.aaron1011.mctester.McTester;
import pw.aaron1011.mctester.TestUtils;
import pw.aaron1011.mctester.framework.TesterHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SpongeProxy implements InvocationHandler {

    public Object realObject;

    public SpongeProxy(Object realObject) {
        this.realObject = realObject;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) {
        if (method.getName().equals("toString") && method.getDeclaringClass().equals(Object.class)) {
            return "SpongeProxy(" + this.realObject + ")";
        }
        InvocationData data = new InvocationData(this.realObject, method, objects);
        McTester.INSTANCE.handler.addInvocation(data);

        TestUtils.waitForSignal();

        return data.response;
    }
}
