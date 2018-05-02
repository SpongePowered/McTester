package org.spongepowered.mctester.test.internal;

import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.runners.model.FrameworkMethod;
import org.spongepowered.mctester.main.appclass.ErrorSlot;

public class InvokeMethodWrapper extends InvokeMethod {

    private ErrorSlot errorSlot;

    public InvokeMethodWrapper(FrameworkMethod testMethod, Object target, ErrorSlot errorSlot) {
        super(testMethod, target);
        this.errorSlot = errorSlot;
    }

    @Override
    public void evaluate() throws Throwable {
        this.errorSlot.clear();
        super.evaluate();
        errorSlot.throwIfSet();
    }
}
