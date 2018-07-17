package org.spongepowered.mctester.api.junit;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.util.List;

public interface IJunitRunner {

    void run(RunNotifier notifier);

    void validateConstructor(List<Throwable> errors);

    void validatePublicVoidNoArgMethods(Class<? extends Annotation> annotation,
            boolean isStatic, List<Throwable> errors);

    TestClass createTestClass(Class<?> testClass);

    Object createTest() throws Exception;

    Statement methodInvoker(FrameworkMethod method, Object test);

}
