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
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

import java.util.List;

/**
 * JUnit Runner which runs tests inside the ClassLoader of the Minecraft Server.
 *
 * @author Michael Vorburger
 * @author Aaron1011
 */
public class MinecraftRunner extends BlockJUnit4ClassRunner {

	// We deliberately don't set the type to RealJunitRunner, since we load it
	// on the LaunchClassLoader
	private static MinecraftServerStarter starter = MinecraftServerStarter.INSTANCE();
	private static IJunitRunner realJUnitRunner;

	public MinecraftRunner(Class<?> testClass) throws InitializationError {
		super(initializeServer(testClass));
	}

	// This is done like this just so that we can run stuff before invoking the parent constructor
	private static Class<?> initializeServer(Class<?> testClass) throws InitializationError {
		String existing = System.getProperty("fml.coreMods.load");
		if (existing == null) {
			existing = "";
		}

		System.setProperty("fml.coreMods.load", existing + "," + "org.spongepowered.mod.SpongeCoremod");
		try {
			if (!starter.isRunning()) {
				starter.startServer();
				starter.waitForServerStartupCompletion();
			}

			Class<?> realJUnit = Class.forName("org.spongepowered.mctester.internal.RealJUnitRunner", true, Launch.classLoader);
			realJUnitRunner = (IJunitRunner) realJUnit.getConstructor(Class.class).newInstance(testClass);
		} catch (Throwable e) {
			throw new InitializationError(e);
		}
		return testClass;
	}

	@Override
	public void run(RunNotifier notifier) {
		realJUnitRunner.run(notifier);
	}

	@Override
	protected void validateConstructor(List<Throwable> errors) {
		realJUnitRunner.validateConstructor(errors);
	}

	@Override
	public TestClass createTestClass(Class<?> testClass) {
		return realJUnitRunner.createTestClass(testClass);
	}

	@Override
	public Object createTest() throws Exception {
		return realJUnitRunner.createTest();
	}

	@Override
	public Statement methodInvoker(FrameworkMethod method, Object test) {
		return realJUnitRunner.methodInvoker(method, test);
	}

}
