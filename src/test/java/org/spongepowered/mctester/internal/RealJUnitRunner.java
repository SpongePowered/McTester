package org.spongepowered.mctester.internal;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.spongepowered.api.Game;
import org.spongepowered.mctester.internal.framework.Client;
import org.spongepowered.mctester.internal.framework.TesterManager;
import org.spongepowered.mctester.junit.DefaultMinecraftRunnerOptions;
import org.spongepowered.mctester.junit.IJunitRunner;
import org.spongepowered.mctester.junit.MinecraftRunnerOptions;
import org.spongepowered.mctester.junit.MinecraftServerStarter;
import org.spongepowered.mctester.junit.RunnerEvents;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RealJUnitRunner extends BlockJUnit4ClassRunner implements IJunitRunner {

    public static TesterManager manager = new TesterManager();
    private MinecraftRunnerOptions options;
    private Thread testThread = Thread.currentThread();

    /**
     * Creates a BlockJUnit4ClassRunner to run {@code klass}
     *
     * @throws InitializationError if the test class is malformed.
     */
    public RealJUnitRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        this.options = testClass.getAnnotation(MinecraftRunnerOptions.class);
        if (this.options == null) {
            this.options = new DefaultMinecraftRunnerOptions();
        }
    }


    @Override
    public void run(RunNotifier notifier) {
        super.run(notifier);

        // We want JUnit to shut down the VM, not Minecraft, so
        // we need to bypass FMLSecurityManager

        // ForceShutdownHandler intercepts ExitTrappedException that will eventually be thrown
        // by JUnit attemping to shut down the VM. It forcible terminates the VM
        // through our TerminateVM class, which bypasses FMLSecurityManager
        Thread.setDefaultUncaughtExceptionHandler(new ForceShutdownHandler());

        if (this.options.exitMinecraftOnFinish()) {
            this.shutDownMinecraft();
        } else {
            RunnerEvents.waitForGameClosed();
        }
    }

    private void shutDownMinecraft() {
        try {
            Minecraft.getMinecraft().shutdown();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void validateConstructor(List<Throwable> errors) {
        super.validateOnlyOneConstructor(errors);
        this.validateSingleArgConstructor(errors);
    }

    private void validateSingleArgConstructor(List<Throwable> errors) {
        // This class is loaded by JUnit, which uses the AppClassLoader. However, our test
        // class is loaded using LaunchClassLoader. To avoid dealing with this, we just compare
        // the fully-qualified names of the classes.
        List<String> parameters = Arrays.stream(getTestClass().getOnlyConstructor().getParameterTypes()).map(Class::getName).collect(Collectors.toList());
        List<String> expectedParameters = Lists.newArrayList(TestUtils.class.getName());
        if (!parameters.equals(expectedParameters)) {
            errors.add(new Exception(String.format("Test class constructor has unexpected parameters: expected %s, but found %s",
                    expectedParameters, parameters)));
        }

    }

    @Override
    public TestClass createTestClass(Class<?> testClass) {
        try {
            ClassLoader classLoader = MinecraftServerStarter.INSTANCE().getMinecraftServerClassLoader();
            Class<?> testClassFromMinecraftClassLoader = Class.forName(testClass.getName(), true, classLoader);
            return super.createTestClass(testClassFromMinecraftClassLoader);
        } catch (ClassNotFoundException e) {
            // This really should never happen..
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object createTest() throws Exception {
        Object object = getTestClass().getOnlyConstructor().newInstance(this.manager);
        return object;
    }

    @Override
    public Statement methodInvoker(FrameworkMethod method, Object test) {
        return new InvokeMethodWrapper(method, test, this.manager.errorSlot);
    }
}
