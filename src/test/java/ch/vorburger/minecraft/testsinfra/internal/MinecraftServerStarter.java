package ch.vorburger.minecraft.testsinfra.internal;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import com.googlecode.junittoolbox.PollingWait;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

/**
 * Helper to programmatically manage a Minecraft server.
 *
 * @author Michael Vorburger
 */
public class MinecraftServerStarter {

	// This is intentionally an Object and not org.spongepowered.api.Game,
	// because that would lead to ClassCastException at this stage here.
	private Object game;
	private LaunchClassLoader minecraftServerClassLoader;

	// https://github.com/michaeltamm/junit-toolbox
	// https://junit-toolbox.googlecode.com/git/javadoc/com/googlecode/junittoolbox/PollingWait.html
	// Alternative: https://github.com/jayway/awaitility
	// Alternative: https://github.com/jhalterman/concurrentunit
	private PollingWait waiter = new PollingWait().timeoutAfter(60, TimeUnit.SECONDS).pollEvery(200, TimeUnit.MILLISECONDS);

	private static MinecraftServerStarter INSTANCE = new MinecraftServerStarter();

	public static MinecraftServerStarter INSTANCE() {
		return INSTANCE;
	}

	private MinecraftServerStarter() { }

	/**
	 * Starts Minecraft Server.
	 *
	 * This launches a background thread.
	 *
	 * @throws Throwable
	 */
	public void startServer() throws Throwable {
		String[] args = new String[] { "--tweakClass", "org.spongepowered.vanilla.launch.server.VanillaServerTweaker", "--noCoreSearch" };
		// TODO instead ch.vorburger.minecraft.testsinfra.GradleStartTestServer.getTweakClass()
		//new GradleStartTestServer().launch(args);
		Class clazz = Class.forName("GradleStart");
		clazz.getMethod("main", String[].class).invoke(null, (Object) new String[0]);
	}

	/**
	 * Waits for Minecraft Server to have successfully fully started.
	 *
	 * @throws Throwable
	 */
	public void waitForServerStartupCompletion() throws Throwable {
		// we CANNOT use TestsRunnerPlugin.isServerStarted == true
		// because the TestsRunnerPlugin will get loaded by a net.minecraft.launchwrapper.LaunchClassLoader,
		// so it's static field won't be the same as the one in our current classloader,
		// but we can work around this problem, like this:
		ClassLoader minecraftLaunchClassLoader = internalGetMinecraftServerClassLoader();
		Thread.currentThread().setContextClassLoader(minecraftLaunchClassLoader);

		Class<?> testsRunnerPluginClassFromLaunchClassLoader = minecraftLaunchClassLoader.loadClass(TestsRunnerPlugin.class.getName());
		Field isServerStartedField = testsRunnerPluginClassFromLaunchClassLoader.getDeclaredField("isServerStarted");
		isServerStartedField.setAccessible(true);

		// logger.info("waitForServerStartupCompletion: isServerStarted == true [{}]", testsRunnerPluginClassFromLaunchClassLoader.getClassLoader());
		waiter.until(() -> Boolean.TRUE.equals(isServerStartedField.get(null)));

		Field gameField = testsRunnerPluginClassFromLaunchClassLoader.getDeclaredField("game");
		gameField.setAccessible(true);
		game = /* DO NOT (Game) */ gameField.get(null);
	}

	public Object getGame() {
		checkIfStarted();
		return game;
	}

	public ClassLoader getMinecraftServerClassLoader() {
		checkIfStarted();
		return internalGetMinecraftServerClassLoader();
	}

	private ClassLoader internalGetMinecraftServerClassLoader() {
		if (minecraftServerClassLoader == null) {
			minecraftServerClassLoader = Launch.classLoader;
			// This is *VERY* important, because without this
			// JUnit will be vey unhappy when in MinecraftTestRunner
			// we replace the class under test with the one from the
			// Minecraft ClassLoader instead of the parent one into
			// which JUnit Framework classes were already loaded
			minecraftServerClassLoader.addClassLoaderExclusion("junit.");
			minecraftServerClassLoader.addClassLoaderExclusion("org.junit.");
		}
		return minecraftServerClassLoader;
	}

	private void checkIfStarted() throws IllegalStateException {
		if (!isRunning())
			throw new IllegalStateException("Minecraft Server has not yet started (or already shut down)");
	}

	public boolean isRunning() {
		return game != null;
	}
}
