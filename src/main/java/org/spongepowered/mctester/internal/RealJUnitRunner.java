package org.spongepowered.mctester.internal;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.launchwrapper.Launch;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.spongepowered.api.Sponge;
import org.spongepowered.asm.lib.ClassReader;
import org.spongepowered.mctester.internal.asm.MainThreadChecker;
import org.spongepowered.mctester.internal.framework.TesterManager;
import org.spongepowered.mctester.internal.world.CurrentWorld;
import org.spongepowered.mctester.junit.DefaultScreenshotOptions;
import org.spongepowered.mctester.junit.DefaultWorldOptions;
import org.spongepowered.mctester.junit.IJunitRunner;
import org.spongepowered.mctester.junit.MinecraftServerStarter;
import org.spongepowered.mctester.junit.RunnerEvents;
import org.spongepowered.mctester.junit.ScreenshotOptions;
import org.spongepowered.mctester.junit.UseSeparateWorld;
import org.spongepowered.mctester.junit.WorldOptions;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RealJUnitRunner extends BlockJUnit4ClassRunner implements IJunitRunner, InvokerCallback, StatusCallback {

    public static GlobalSettings GLOBAL_SETTINGS = new GlobalSettings();

    private TesterManager testerManager;
    private Thread testThread = Thread.currentThread();
    private boolean initialized;
    private File gamedir;
    private CurrentWorld currentWorld;
    private CurrentWorld tempWorld = null;
    private RunNotifier runNotifier;

    private TestStatus globalTestStatus = new TestStatus(this);
    private TestStatus currentTestStatus;

    private WorldOptions worldOptions;
    private ScreenshotOptions screenshotOptions;

    private TestActions testActions;

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

        //MainThreadChecker.checkClass(testClass);

        this.worldOptions = testClass.getAnnotation(WorldOptions.class);
        if (this.worldOptions == null) {
            this.worldOptions = new DefaultWorldOptions();
        }

        this.screenshotOptions = testClass.getAnnotation(ScreenshotOptions.class);
        if (this.screenshotOptions == null) {
            this.screenshotOptions = new DefaultScreenshotOptions();
        }

        this.currentWorld = new CurrentWorld();
    }


    @Override
    public void run(RunNotifier notifier) {
        Thread.setDefaultUncaughtExceptionHandler(new ForceShutdownHandler(this.globalTestStatus));
        RemoveSecurityManager.clearSecurityManager();

        notifier.addListener(this.globalTestStatus);
        this.runNotifier = notifier;
        super.run(notifier);

        this.testActions.tryDeleteWorldGlobal(this.currentWorld);
        this.testActions.tryTakeScreenShotGlobal(this.getWorldName());
    }

    @Override
    public void onFinished() {
        /*if (this.shouldDeleteWorldGlobal()) {
            this.currentWorld.deleteWorld();
        }*/

    }


    private void performInit() {
        if (!this.initialized) {
            RunnerEvents.waitForClientInit();

            this.joinNewWorld();

            this.testerManager = new TesterManager();
            this.testActions = new TestActions(this.testerManager, this.worldOptions, this.screenshotOptions, this.globalTestStatus);
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

    private void checkTestBytecode(Class<?> testClass) {
        try {
            byte[] bytecode = Launch.classLoader.getClassBytes(testClass.getName());
            ClassReader reader = new ClassReader(bytecode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object createTest() throws Exception {
        this.performInit();
        Object object = getTestClass().getOnlyConstructor().newInstance(this.testerManager);
        return object;
    }

    @Override
    public Statement methodInvoker(FrameworkMethod method, Object test) {
        return new InvokeMethodWrapper(method, test, this);
    }

    private String getWorldName() {
        return getTestClass().getJavaClass().getSimpleName() + "-";
    }

    @Override
    public void beforeInvoke(FrameworkMethod method) {
        if (method.getAnnotation(UseSeparateWorld.class) != null) {
            System.err.println("Creating new world for: " + method.getMethod());

            this.tempWorld = new CurrentWorld();
            this.tempWorld.joinNewWorld(CUSTOM_WORLD_PREFIX + this.getWorldName() + method.getName() + "-");
        }

        this.currentTestStatus = new TestStatus(null);
        this.testerManager.beforeTest();
    }

    @Override
    public void afterInvoke(FrameworkMethod method) throws Throwable {
        this.testerManager.checkAndClearErrorSlots();

        if (this.tempWorld != null) {
            this.testActions.tryDeleteTempWorld(method, this.currentTestStatus, this.tempWorld);
            this.tempWorld = null;
        }

        this.testActions.tryTakeScreenShotCustom(method, this.currentTestStatus);

        this.currentTestStatus = null;
    }
}
