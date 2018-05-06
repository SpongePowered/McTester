package org.spongepowered.mctester.internal;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.spongepowered.mctester.internal.framework.TesterManager;
import org.spongepowered.mctester.internal.world.CurrentWorld;
import org.spongepowered.mctester.junit.DefaultWorldOptions;
import org.spongepowered.mctester.junit.IJunitRunner;
import org.spongepowered.mctester.junit.WorldOptions;
import org.spongepowered.mctester.junit.MinecraftServerStarter;
import org.spongepowered.mctester.junit.RunnerEvents;
import org.spongepowered.mctester.junit.UseSeparateWorld;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RealJUnitRunner extends BlockJUnit4ClassRunner implements IJunitRunner, InvokerCallback, StatusCallback {

    public static GlobalSettings GLOBAL_SETTINGS = new GlobalSettings();

    private TesterManager manager;
    private WorldOptions options;
    private Thread testThread = Thread.currentThread();
    private boolean initialized;
    private File gamedir;
    private CurrentWorld currentWorld;
    private TestStatus testStatus = new TestStatus(this);
    private List<CurrentWorld> tempWorlds = new ArrayList<>();
    private boolean inTempWorld;

    private static final String MCTESTER_WORLD_BASE = "MCTester-";
    private static final String NORMAL_WORLD_PREFIX = MCTESTER_WORLD_BASE + "Normal-";
    private static final String CUSTOM_WORLD_PREFIX = MCTESTER_WORLD_BASE + "Custom-";

    /**
     * Creates a BlockJUnit4ClassRunner to run {@code klass}
     *
     * @throws InitializationError if the test class is malformed.
     */
    public RealJUnitRunner(Class<?> testClass) throws InitializationError {
        super(testClass);

        this.options = testClass.getAnnotation(WorldOptions.class);
        if (this.options == null) {
            this.options = new DefaultWorldOptions();
        }
        this.currentWorld = new CurrentWorld();
    }


    @Override
    public void run(RunNotifier notifier) {
        Thread.setDefaultUncaughtExceptionHandler(new ForceShutdownHandler(this.testStatus));
        notifier.addListener(this.testStatus);
        super.run(notifier);

        if (this.shouldDeleteWorld()) {
            this.currentWorld.deleteWorld();
        }
    }

    @Override
    public void onFinished() {
        /*if (this.shouldDeleteWorld()) {
            this.currentWorld.deleteWorld();
        }*/

    }

    private boolean shouldDeleteWorld() {
        return (this.options.deleteWorldOnSuccess() && this.testStatus.succeeded()) || (this.options.deleteWorldOnFailure() && this.testStatus.failed());
    }

    private boolean shouldDeleteTempWorld(FrameworkMethod method, boolean success) {
        WorldOptions options = method.getAnnotation(WorldOptions.class);
        if (options == null) {
            return this.shouldDeleteWorld();
        }
        return (success && options.deleteWorldOnSuccess()) || (!success && options.deleteWorldOnFailure());
    }

    private void performInit() {
        if (!this.initialized) {
            RunnerEvents.waitForClientInit();

            this.joinNewWorld();

            this.manager = new TesterManager();
            this.initialized = true;
        }
    }

    private void joinNewWorld() {
        this.joinNewWorld(NORMAL_WORLD_PREFIX + this.getWorldName());
    }

    private void joinNewWorld(String base) {
        this.currentWorld.joinNewWorld(base);
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
        this.performInit();
        Object object = getTestClass().getOnlyConstructor().newInstance(this.manager);
        return object;
    }

    @Override
    public Statement methodInvoker(FrameworkMethod method, Object test) {
        return new InvokeMethodWrapper(method, test, this.manager.errorSlot, this);
    }

    private String getWorldName() {
        return getTestClass().getJavaClass().getSimpleName() + "-";
    }

    @Override
    public void beforeInvoke(FrameworkMethod method) {
        if (method.getAnnotation(UseSeparateWorld.class) != null) {
            System.err.println("Creating new world for: " + method.getMethod());

            CurrentWorld tempWorld = new CurrentWorld();
            tempWorld.joinNewWorld(CUSTOM_WORLD_PREFIX + this.getWorldName() + method.getName() + "-");

            this.tempWorlds.add(tempWorld);
            this.inTempWorld = true;
        }
    }

    @Override
    public void afterInvoke(FrameworkMethod method, Throwable throwable) {
        boolean success = throwable == null;
        if (this.inTempWorld && this.shouldDeleteTempWorld(method, success)) {
            CurrentWorld tempWorld = this.tempWorlds.remove(this.tempWorlds.size() - 1);
            tempWorld.deleteWorld();
        }
    }
}
