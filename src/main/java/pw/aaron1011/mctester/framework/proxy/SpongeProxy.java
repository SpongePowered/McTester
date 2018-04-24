package pw.aaron1011.mctester.framework.proxy;

import com.google.common.util.concurrent.SettableFuture;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SpongeProxy implements InvocationHandler {

    public Object realObject;
    public BlockingQueue<InvocationData> queue;

    public SpongeProxy(Object realObject, BlockingQueue<InvocationData> queue) {
        this.realObject = realObject;
        this.queue = queue;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        InvocationData data = new InvocationData(method, objects);
        this.queue.add(data);
        data.wait();
        return data.response;
    }
}
