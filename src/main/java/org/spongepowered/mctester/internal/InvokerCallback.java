package org.spongepowered.mctester.internal;

import org.junit.runners.model.FrameworkMethod;

public interface InvokerCallback {

    void beforeInvoke(FrameworkMethod method);

    void afterInvoke(FrameworkMethod method) throws Throwable ;

}
