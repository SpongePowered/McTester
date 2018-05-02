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
package org.spongepowered.mctester.test.internal;

import com.google.common.collect.Lists;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.spongepowered.mctester.main.appclass.ErrorSlot;
import org.spongepowered.mctester.test.internal.internal.MinecraftServerStarter;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

import org.spongepowered.api.Game;
import org.spongepowered.mctester.main.TestUtils;
import org.spongepowered.mctester.main.framework.Client;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JUnit Runner which runs tests inside the ClassLoader of the Minecraft Server.
 *
 * @author Michael Vorburger
 */
public class MinecraftRunner extends BlockJUnit4ClassRunner {

	static MinecraftServerStarter starter = MinecraftServerStarter.INSTANCE();
    private ErrorSlot errorSlot = new ErrorSlot();
	private LCLBridge lclBridge;

	public MinecraftRunner(Class<?> testClass) throws InitializationError {
		super(initializeServer(testClass));
		// Create the LCLBridge after the server is running
		this.lclBridge = new LCLBridge(this);
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
		} catch (Throwable e) {
			throw new InitializationError(e);
		}
		return testClass;
	}


	@Override
	public void run(RunNotifier notifier) {
		super.run(notifier);
		this.lclBridge.shutDownMinecraft();
	}

	@Override
	protected void validateConstructor(List<Throwable> errors) {
		super.validateOnlyOneConstructor(errors);
		this.validateThreeArgConstructor(errors);
	}

	private void validateThreeArgConstructor(List<Throwable> errors) {
		// This class is loaded by JUnit, which uses the AppClassLoader. However, our test
		// class is loaded using LaunchClassLoader. To avoid dealing with this, we just compare
		// the fully-qualified names of the classes.
		List<String> parameters = Arrays.stream(getTestClass().getOnlyConstructor().getParameterTypes()).map(Class::getName).collect(Collectors.toList());
		List<String> expectedParameters = Lists.newArrayList(Game.class.getName(), Client.class.getName(), TestUtils.class.getName());
		if (!parameters.equals(expectedParameters)) {
			errors.add(new Exception(String.format("Test class constructor has unexpected parameters: expected %s, but found %s",
					expectedParameters, parameters)));
		}

	}

	@Override
	protected TestClass createTestClass(Class<?> testClass) {
		try {
			ClassLoader classLoader = starter.getMinecraftServerClassLoader();
			Class<?> testClassFromMinecraftClassLoader = Class.forName(testClass.getName(), true, classLoader);
			return super.createTestClass(testClassFromMinecraftClassLoader);
		} catch (ClassNotFoundException e) {
			// This really should never happen..
			throw new RuntimeException(e);
		}
	}

	@Override
	protected Object createTest() throws Exception {
		Object object = this.lclBridge.createTest(this.getTestClass());
		return object;
	}

	@Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
		return new InvokeMethodWrapper(method, test, this.errorSlot);
	}

}
