package org.spongepowered.mctester.internal;

import org.spongepowered.api.Sponge;
import org.spongepowered.mctester.internal.appclass.ErrorSlot;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.runners.model.FrameworkMethod;
import org.spongepowered.mctester.junit.UseSeparateWorld;

public class InvokeMethodWrapper extends InvokeMethod {

    private FrameworkMethod method;
    private InvokerCallback callback;

    public InvokeMethodWrapper(FrameworkMethod testMethod, Object target, InvokerCallback callback) {
        super(testMethod, target);
        this.method = testMethod;
        this.callback = callback;
    }

    @Override
    public void evaluate() throws Throwable {
        this.callback.beforeInvoke(this.method);

        try {
            super.evaluate();
        } finally {
            Sponge.getEventManager().unregisterPluginListeners(McTesterDummy.INSTANCE);
            this.callback.afterInvoke(this.method);
        }
    }
}
