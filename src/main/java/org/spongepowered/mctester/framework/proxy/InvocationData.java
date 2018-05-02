package org.spongepowered.mctester.framework.proxy;

import org.spongepowered.mctester.message.toserver.MessageRPCResponse;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Callable;

public class InvocationData implements Callable<Object>  {

    public Object realObject;
    public Method method;
    public List<?> args;
    public Object response;

    public InvocationData(Object realObject, Method method, List<?> args) {
        this.realObject = realObject;
        this.method = method;
        this.args = args;
    }


    @Override
    public Object call() throws Exception {
        this.response = this.method.invoke(this.realObject, this.args.toArray(new Object[this.args.size()]));
        return this.response;
    }

    public MessageRPCResponse makeResponse() throws Exception {
        this.response = this.call();
        return new MessageRPCResponse(this.response, this.method.getReturnType());
    }
}
