package org.spongepowered.mctester.internal;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.spongepowered.mctester.internal.framework.TesterManager;
import org.spongepowered.mctester.junit.DefaultMinecraftRunnerOptions;
import org.spongepowered.mctester.junit.IJunitRunner;
import org.spongepowered.mctester.junit.MinecraftRunnerOptions;
import org.spongepowered.mctester.junit.MinecraftServerStarter;
import org.spongepowered.mctester.junit.RunnerEvents;
import org.spongepowered.mctester.junit.UseSeparateWorld;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class RealJUnitRunner extends BlockJUnit4ClassRunner implements IJunitRunner {

    public static GlobalSettings GLOBAL_SETTINGS = new GlobalSettings();

    private TesterManager manager;
    private MinecraftRunnerOptions options;
    private Thread testThread = Thread.currentThread();
    private boolean initialized;
    private File gamedir;
    private String currentWorld;
    private FailureDetector failureDetector = new FailureDetector();

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

    public void joinNewWorld() {
        this.exitToMainMenu();

        try {
            Minecraft.getMinecraft().addScheduledTask(new Runnable() {

                @Override
                public void run() {
                    long seed = new Random().nextLong();
                    RealJUnitRunner.this.currentWorld = "MCTestWorld-" + String.valueOf(seed).substring(0, 5);

                    WorldSettings worldsettings = new WorldSettings(seed, GameType.CREATIVE, false, false, WorldType.FLAT);
                    Minecraft.getMinecraft().launchIntegratedServer(RealJUnitRunner.this.currentWorld, RealJUnitRunner.this.currentWorld, worldsettings);
                }
            }).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void exitToMainMenu() {
        // We deliberately avoid using Minecraft.addScheduledTask here.
        //
        // When we call World.sendQuittingDisconnectingPacket,
        // the client thread will block waiting for the Netty channel
        // to close.
        //
        // The client controls all access to its scheduledTasks list -
        // both adding and executing tasks - with
        // a synchronized(Minecraft.scheduledTasks) block
        //
        // If we call sendQuittingDisconnectingPacket in a scheduled
        // task, we'll end up blcoking on the channel closure while
        // in a synchronized(Minecraft.scheduledTasks). However, the
        // Netty client thread can also enter a synchronized(Minecraft.scheduledTasks)
        // block, since it schedulers packet handlers on the main thread.
        // This means that we can very easily end up with a deadlock
        // if the Netty thread tries to process a packet while we're
        // waiting for the channel to close
        //
        // To avoid the deadlock, we do what Vanilla does - call
        // sendQuittingDisconnectingPacket from a GUI.
        // In Vanilla, this is either a GuiGameOver or a GuiInGameOMenu
        // Here, we create a simple GuiScreen subclass to run our code
        // on the client thread.
        CompletableFuture<Void> atMainMenu = new CompletableFuture<>();
        Minecraft.getMinecraft().displayGuiScreen(new GuiScreen() {
            @Override
            public void updateScreen() {

                if (Minecraft.getMinecraft().isIntegratedServerRunning()) {
                    Minecraft.getMinecraft().world.sendQuittingDisconnectingPacket();
                    Minecraft.getMinecraft().loadWorld((WorldClient) null);
                }

                Minecraft.getMinecraft().displayGuiScreen(new GuiMainMenu());
                atMainMenu.complete(null);
            }
        });

        Futures.getUnchecked(atMainMenu);
        RunnerEvents.resetPlayerJoined();
    }


    @Override
    public void run(RunNotifier notifier) {
        Thread.setDefaultUncaughtExceptionHandler(new ForceShutdownHandler(this.failureDetector));
        notifier.addListener(this.failureDetector);

        super.run(notifier);
        this.exitToMainMenu();

        // We want JUnit to shut down the VM, not Minecraft, so
        // we need to bypass FMLSecurityManager

        // ForceShutdownHandler intercepts ExitTrappedException that will eventually be thrown
        // by JUnit attemping to shut down the VM. It forcible terminates the VM
        // through our TerminateVM class, which bypasses FMLSecurityManager

        // We keep the game open if at least one test doesn't want to shut it down
        /*if (this.options.exitMinecraftOnFinish()) {
            this.shutDownMinecraft();
        }*/
    }

    private void performInit() {
        if (!this.initialized) {
            RunnerEvents.waitForClientInit();

            this.joinNewWorld();
            RunnerEvents.waitForPlayerJoin();

            this.manager = new TesterManager();
            this.initialized = true;
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
        this.performInit();
        Object object = getTestClass().getOnlyConstructor().newInstance(this.manager);
        return object;
    }

    @Override
    public Statement methodInvoker(FrameworkMethod method, Object test) {
        if (method.getAnnotation(UseSeparateWorld.class) != null) {
            System.err.println("Creating new world for: " + method.getMethod());
            this.joinNewWorld();
        }
        return new InvokeMethodWrapper(method, test, this.manager.errorSlot);
    }
}
