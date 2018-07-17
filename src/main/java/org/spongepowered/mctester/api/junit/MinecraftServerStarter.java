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
package org.spongepowered.mctester.api.junit;

import net.minecraft.client.Minecraft;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.spongepowered.mctester.api.RunnerEvents;
import org.spongepowered.mctester.installer.SpongeInstaller;
import org.spongepowered.mctester.internal.RealJUnitRunner;

import java.io.File;

/**
 * Helper to programmatically manage a Minecraft server.
 *
 * @author Michael Vorburger
 */
public class MinecraftServerStarter {

	private LaunchClassLoader minecraftServerClassLoader;

	private static MinecraftServerStarter INSTANCE = new MinecraftServerStarter();
	private boolean started;

	public static MinecraftServerStarter INSTANCE() {
		return INSTANCE;
	}

	private MinecraftServerStarter() { }

	/**
	 * Starts the Minecraft Client
	 *
	 * This launches a background thread.
	 *
	 */
	public void startClient() {
		try {
			if (this.started) {
				return;
			}

			this.started = true;

			boolean spongeInstalled = this.isSpongeInstalled();
			if (!spongeInstalled) {
				this.installSponge();
			} else {
				String existing = System.getProperty("fml.coreMods.load", "");
				System.setProperty("fml.coreMods.load", existing + "," + "org.spongepowered.mod.SpongeCoremod");
			}

			System.setProperty("mixin.env.disableRefMap", "true");
			System.setProperty("fml.readTimeout", "0");
			//System.setProperty("mixin.debug.verbose", "true");

			String[] args = new String[] {"--tweakClass", "org.spongepowered.mctester.api.MinecraftRunnerTweaker", "--gameDir",
					RealJUnitRunner.GLOBAL_SETTINGS.getGameDir().getAbsolutePath()};
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

			wrapperThread.setUncaughtExceptionHandler(((thread, throwable) -> {
				System.err.println("Uncaught exception for Minecraft client thread!");
				throwable.printStackTrace();
				RunnerEvents.fatalError(throwable);
			}));

			Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
				System.err.println("Uncaight exception for JUnit main thread!");
				throwable.printStackTrace();
				System.exit(-1);
			});

			Runtime.getRuntime().addShutdownHook(new Thread() {

				@Override
				public void run() {
					Minecraft.getMinecraft().shutdown();
				}
			});

			wrapperThread.start();
		} catch (Throwable throwable) {
			// This method is invoked from when JUnit is just starting to build the runner.
			// If an exception gets thrown, it would normally be captured and printed later by JUnit.
			// However, the main thread will end up blocked forever on LaunchClassLaoder being avaialble,
			// since the client will never start
			throwable.printStackTrace();
			throw new RuntimeException("Error starting Minecraft!", throwable);
		}

	}

	public ClassLoader getMinecraftServerClassLoader() {
	    RunnerEvents.waitForLaunchClassLoaderFuture();
		return internalGetMinecraftServerClassLoader();
	}

	private ClassLoader internalGetMinecraftServerClassLoader() {
		if (minecraftServerClassLoader == null) {
			minecraftServerClassLoader = Launch.classLoader;
		}
		return minecraftServerClassLoader;
	}

	private boolean isSpongeInstalled() {
		return this.getClass().getClassLoader().getResource("org/spongepowered/common/launch/SpongeLaunch.class") != null;
	}

	private void installSponge() {
		System.err.println("Downloading latest SpongeForge!");
		File outputDir = new File(RealJUnitRunner.GLOBAL_SETTINGS.getGameDir(), "mods");
		outputDir.mkdirs();

		new SpongeInstaller().downloadLatestSponge(outputDir);
        System.err.println("Downloaded latest SpongeForge!");

	}

}
