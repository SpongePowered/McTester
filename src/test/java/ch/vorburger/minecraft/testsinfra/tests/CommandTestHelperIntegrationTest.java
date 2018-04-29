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

import ch.vorburger.minecraft.testsinfra.MinecraftRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;

/**
 * Integration test for CommandTestHelper.
 *
 * @author Michael Vorburger
 */
@RunWith(MinecraftRunner.class)
public class CommandTestHelperIntegrationTest {

	// TODO @Inject
	public Game game;

	/*@Test(expected=AssertionError.class)
	public void badCommandShouldFail() {
		CommandTestHelper h = new CommandTestHelper(game);
		h.process("badNonExistantMinecraftSlashCommand");
	}*/

	@Test public void helpHelp() {
		Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "sponge version");
		/*CommandTestHelper h = new CommandTestHelper(game);
		CommandResultWithChat result = h.process("help say");
		h.assertContainsIgnoreCase(result.getChat(), "/say <message ...>");*/
	}

	/*@Test public void testPluginBadCommand() {
		CommandTestHelper h = new CommandTestHelper(game);
		try {
			h.process(TestPlugin.TEST_BAD_COMMAND);
			fail("This should have failed :(");
		} catch (AssertionError e) {
			assertTrue(e.getMessage(), e.getMessage().equals("Error occurred while executing command: %s"));
			// TODO how-to? assertTrue(e.getMessage(), e.getMessage().equals("bad boy command failure"));
			// TODO how-to? getCause(), IllegalStateException, getMessage() = "root cause IllegalStateException"
		}
	}*/

}
