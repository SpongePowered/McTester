/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.mctester.junit;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

/**
 * Helper to programmatically manage a Minecraft server.
 *
 * @author Michael Vorburger
 */
public class MinecraftServerStarter {

	private LaunchClassLoader minecraftServerClassLoader;

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
		String[] args = new String[] { "--tweakClass", "org.spongepowered.mctester.junit.MinecraftRunnerTweaker", "--gameDir", "/home/aaron/repos/sponge/dev/run/mctester" };
		// TODO instead ch.vorburger.minecraft.testsinfra.GradleStartTestServer.getTweakClass()
		//new GradleStartTestServer().launch(args);
		Class clazz = Class.forName("GradleStart");
		Thread wrapperThread = new Thread(() -> {
			try {
				clazz.getMethod("main", String[].class).invoke(null, (Object) args);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		wrapperThread.start();
	}

	/**
	 * Waits for Minecraft Server to have successfully fully started.
	 *
	 * @throws Throwable
	 */
	public void waitForServerStartupCompletion() throws Throwable {
		RunnerEvents.waitForPlayerJoin();
/*		// we CANNOT use TestsRunnerPlugin.isServerStarted == true
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
		gameField.setAccessible(true);*/
		//game = /* DO NOT (Game) */ gameField.get(null);
	}

	public ClassLoader getMinecraftServerClassLoader() {
		checkIfStarted();
		return internalGetMinecraftServerClassLoader();
	}

	private ClassLoader internalGetMinecraftServerClassLoader() {
		if (minecraftServerClassLoader == null) {
			minecraftServerClassLoader = Launch.classLoader;
		}
		return minecraftServerClassLoader;
	}

	private void checkIfStarted() throws IllegalStateException {
		if (!isRunning()) {
			throw new IllegalStateException("Minecraft Server has not yet started (or already shut down)");
		}
	}

	public boolean isRunning() {
		return RunnerEvents.hasPlayerJoined();
	}

}
