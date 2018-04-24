package pw.aaron1011.mctester.framework.proxy;

import pw.aaron1011.mctester.McTester;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InvocationData {

    public Object realObject;
    public Method method;
    public Object[] args;
    public Object response;

    public InvocationData(Object realObject, Method method, Object[] args) {
        this.realObject = realObject;
        this.method = method;
        this.args = args;
    }

    public void execute() throws InvocationTargetException, IllegalAccessException {
        Object response = this.method.invoke(this.realObject, this.args);
        if (McTester.shouldProxy(realObject)) {

        }
    }
}
