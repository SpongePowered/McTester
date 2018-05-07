package org.spongepowered.mctester.internal;

import org.spongepowered.api.Sponge;
import org.spongepowered.mctester.internal.appclass.ErrorSlot;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.runners.model.FrameworkMethod;
import org.spongepowered.mctester.junit.UseSeparateWorld;

public class InvokeMethodWrapper extends InvokeMethod {

    private ErrorSlot errorSlot;
    private FrameworkMethod method;
    private InvokerCallback callback;

    public InvokeMethodWrapper(FrameworkMethod testMethod, Object target, ErrorSlot errorSlot, InvokerCallback callback) {
        super(testMethod, target);
        this.errorSlot = errorSlot;
        this.method = testMethod;
        this.callback = callback;
    }

    @Override
    public void evaluate() throws Throwable {
        this.errorSlot.clear();
        this.callback.beforeInvoke(this.method);

        Throwable throwable = null;

        try {
            super.evaluate();
        } catch (Throwable t) {
            throwable = t;
            throw throwable;
        } finally {
            Sponge.getEventManager().unregisterPluginListeners(McTesterDummy.INSTANCE);

            Throwable captured = throwable;
            if (captured == null) {
                captured = errorSlot.getStored();
            }
            this.callback.afterInvoke(this.method, captured);
        }
        errorSlot.throwIfSet();
    }
}
