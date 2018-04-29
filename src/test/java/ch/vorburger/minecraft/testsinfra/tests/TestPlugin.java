/*
 * This file is part of Michael Vorburger's SwissKnightMinecraft project, licensed under the MIT License (MIT).
 *
 * Copyright (c) Michael Vorburger <http://www.vorburger.ch>
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
package ch.vorburger.minecraft.testsinfra.tests;

import com.google.inject.Inject;
import org.spongepowered.api.Game;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

/**
 * Tests Plugin.
 *
 * @author Michael Vorburger
 */
@Plugin(id = "test_self", name = "Integration Tests Self Test", description = "Whatever", version = "1.0")
public class TestPlugin {

	static final String TEST_BAD_COMMAND = "testBadCommand";

	@Inject Game game;
	@Inject PluginContainer plugin;
	//private CommandMapping commandMapping;

	/*@Listener
	public final void onGameInitialization(GameInitializationEvent event) {
		CommandSpec commandSpec = CommandSpec.builder()
				.description(Texts.of("Hello World Command"))
				.permission("myplugin.command.helloworld")
				.executor(new CommandExecutor() {
					@Override
					public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
						throw new InvocationCommandException(Texts.of("bad boy command failure"), new IllegalStateException("root cause IllegalStateException"));
					}
				})
				.build();

		commandMapping = game.getCommandDispatcher().register(plugin, commandSpec, TEST_BAD_COMMAND).get();
	}

	@Listener
	public final void onGameStoppingServerEvent(GameStoppingServerEvent event) {
		game.getCommandDispatcher().removeMapping(commandMapping);
	}*/

}
