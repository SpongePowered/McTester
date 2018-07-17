package org.spongepowered.mctester.internal;

import org.junit.runners.model.Statement;
import org.spongepowered.api.Sponge;
import org.junit.runners.model.FrameworkMethod;

public class InvokeMethodWrapper extends Statement {

    protected FrameworkMethod method;
    protected Object target;
    private InvokerCallback callback;

    public InvokeMethodWrapper(FrameworkMethod testMethod, Object target, InvokerCallback callback) {
        this.method = testMethod;
        this.target = target;
        this.callback = callback;
    }

    @Override
    public void evaluate() throws Throwable {
        this.callback.beforeInvoke(this.method);

        try {
            this.doInvocation();
        } finally {
            Sponge.getEventManager().unregisterPluginListeners(McTesterDummy.INSTANCE);
            this.callback.afterInvoke(this.method);
        }
    }

    protected void doInvocation() throws Throwable {
        this.method.invokeExplosively(this.target);
    }
}
