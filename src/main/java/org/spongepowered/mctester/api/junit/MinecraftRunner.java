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

import net.minecraft.launchwrapper.LaunchClassLoader;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.spongepowered.mctester.api.RunnerEvents;
import org.spongepowered.mctester.api.WorldOptions;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * JUnit Runner which runs tests on a Minecraft singleplayer client
 *
 * The {@link WorldOptions} can be used to configure
 * the runner
 *
 * The follow system properties affect the behavior of all MinecraftRunners:
 *
 * - 'mctester.gamedir' - sets the base directory for running the client. Equivalent
 *   to the '~/.minecraft' directory. Defaults to /tmp/mctester/gamedir
 *
 * - 'mctester.shutdownOnSuccess' - whether or not to shut down Minecraft
 *   automatically if all MinecraftRunner tests pass. Defaults to 'true'
 *
 * - 'mctester.shutdownOnFailure' - whether or not to shut down Minecraft
 *   automatically if at least one MinecraftRunner test failed. Defaults to 'true'
 *
 * - 'mctester.shutdownOnError' - whether ot not to shut down Minecraft
 *   automatically if JUnit did not complete normally due to an unexpected error.
 *   Defaults to 'true'
 *
 *
 * @author Aaron1011
 * @author Michael Vorburger
 */
public class MinecraftRunner extends BlockJUnit4ClassRunner {

	// We deliberately don't set the type to RealJunitRunner, since we load it
	// on the LaunchClassLoader
	private static MinecraftClientStarter starter = MinecraftClientStarter.INSTANCE();
	private IJunitRunner realJUnitRunner;

	private List<Throwable> caughtInitializationErrors = new ArrayList<>();

	public static TestStatus globalTestStatus = new TestStatus();

	public MinecraftRunner(Class<?> testClass) throws InitializationError {
		super(initializeClient(testClass));
	}

	// This is done like this just so that we can run stuff before invoking the parent constructor
	private static Class<?> initializeClient(Class<?> testClass) throws InitializationError {
		try {
			starter.startClient();

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
	protected void validatePublicVoidNoArgMethods(Class<? extends Annotation> annotation,
			boolean isStatic, List<Throwable> errors) {
		realJUnitRunner.validatePublicVoidNoArgMethods(annotation, isStatic, errors);
	}

	@Override
	public TestClass createTestClass(Class<?> testClass) {
		try {
			LaunchClassLoader classLoader = RunnerEvents.waitForLaunchClassLoaderFuture();
			Class<?> realJUnit = Class.forName("org.spongepowered.mctester.internal.RealJUnitRunner", true, classLoader);
			this.realJUnitRunner = (IJunitRunner) realJUnit.getConstructor(Class.class).newInstance(testClass);
		} catch (Exception e) {
			// createTestClass is called from the super() constructor, so our field initializers
			// haven't run yet
			if (this.caughtInitializationErrors == null) {
				this.caughtInitializationErrors = new ArrayList<>();
			}
			if (e instanceof InvocationTargetException) {
				if (e.getCause() instanceof InitializationError) {
					this.caughtInitializationErrors.addAll(((InitializationError) e.getCause()).getCauses());
				} else {
					this.caughtInitializationErrors.add(((InvocationTargetException) e).getTargetException());
				}
			} else {
				this.caughtInitializationErrors.add(e);
			}
			return null;
		}

		return realJUnitRunner.createTestClass(testClass);
	}

	@Override
	protected void collectInitializationErrors(List<Throwable> errors) {
		if (this.caughtInitializationErrors != null && !this.caughtInitializationErrors.isEmpty()) {
			errors.addAll(this.caughtInitializationErrors);
		} else {
			super.collectInitializationErrors(errors);
		}
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
