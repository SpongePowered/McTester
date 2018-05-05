package org.spongepowered.mctester.internal;

import org.spongepowered.api.Sponge;
import org.spongepowered.mctester.internal.appclass.ErrorSlot;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.runners.model.FrameworkMethod;

public class InvokeMethodWrapper extends InvokeMethod {

    private ErrorSlot errorSlot;

    public InvokeMethodWrapper(FrameworkMethod testMethod, Object target, ErrorSlot errorSlot) {
        super(testMethod, target);
        this.errorSlot = errorSlot;
    }

    @Override
    public void evaluate() throws Throwable {
        this.errorSlot.clear();
        try {
            super.evaluate();
        } finally {
            Sponge.getEventManager().unregisterPluginListeners(McTesterDummy.INSTANCE);
        }
        errorSlot.throwIfSet();
    }
}
