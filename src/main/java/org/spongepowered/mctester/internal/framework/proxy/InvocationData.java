package org.spongepowered.mctester.internal.framework.proxy;

import com.google.common.collect.Lists;
import org.spongepowered.mctester.internal.McTester;
import org.spongepowered.mctester.internal.message.toserver.MessageRPCResponse;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Callable;

public class InvocationData implements Callable<Object>  {

    public Object realObject;
    public Method method;
    public List<?> args;
    public Object response;
    private boolean takesCallback;

    public InvocationData(Object realObject, Method method, List<?> args) {
        this.realObject = realObject;
        this.method = method;
        this.args = args;

        if (method.getParameterCount() != 0 && method.getParameterTypes()[method.getParameterCount() - 1] == ResponseCallback.class) {
            this.takesCallback = true;
            if (args.size() == method.getParameterCount() && args.get(args.size() - 1) == null) {
                this.args = Lists.newArrayList(this.args);
                this.args.remove(this.args.size() - 1);
            }
        }
    }


    @Override
    public Object call() throws Exception {
        if (!this.takesCallback) {
            this.response = this.method.invoke(this.realObject, this.args.toArray(new Object[0]));
        } else {
            List<Object> newArgs = Lists.newArrayList(this.args);
            newArgs.add(new ResponseCallback(this.method.getReturnType()));
            this.response = this.method.invoke(this.realObject, newArgs.toArray());
        }
        return this.response;
    }

    public void doResponse() throws Exception {
        this.response = this.call();
        if (!this.takesCallback) {
            new ResponseCallback(this.method.getReturnType()).sendResponse(this.response);
        }
    }

    public String format() {
        return this.method.toString();
    }
}

