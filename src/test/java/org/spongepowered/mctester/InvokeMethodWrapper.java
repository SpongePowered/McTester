package org.spongepowered.mctester;

import org.spongepowered.mctester.old.appclass.ErrorSlot;
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
        super.evaluate();
        errorSlot.throwIfSet();
    }
}
