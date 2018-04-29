package ch.vorburger.minecraft.testsinfra;

public class CommandTestHelperTest {

	/*@Test public void textToString() {
		Text text = Texts.of("something");
		assertEquals("something", new CommandTestHelper(null).toString(text));
	}

	@Test public void translatedTextToString() {
		Text text = Texts.builder(new FixedTranslation("something")).build();
		assertEquals("something", new CommandTestHelper(null).toString(text));
	}

	@Test public void chatToString() {
		Chat chat = () -> Collections.singletonList(Texts.of("something"));
		assertEquals("something", new CommandTestHelper(null).toString(chat));
	}

	@Test(expected=AssertionError.class)
	public void assertDoesNotContainLiteralTextFail() {
		Chat chat = () -> Collections.singletonList(Texts.of("something"));
		new CommandTestHelper(null).assertDoesNotContainIgnoreCase(chat, "Something");
	}

	@Test(expected=AssertionError.class)
	public void assertDoesNotContainStyledTextFail() {
		Chat chat = () -> Collections.singletonList(Texts.builder("somethingRed").style(SpongeTextStyle.of(EnumChatFormatting.STRIKETHROUGH)).build());
		new CommandTestHelper(null).assertDoesNotContainIgnoreCase(chat, "something");
	}

	@Test(expected=AssertionError.class)
	public void assertDoesNotContainTranslationFail() {
		Chat chat = () -> Collections.singletonList(Texts.builder(new FixedTranslation("something")).build());
		new CommandTestHelper(null).assertDoesNotContainIgnoreCase(chat, "something");
	}

	@Test public void assertDoesNotContainPass() {
		Chat chat = () -> Collections.singletonList(Texts.of("anything"));
		new CommandTestHelper(null).assertDoesNotContainIgnoreCase(chat, "something");
	}

	@Test(expected=AssertionError.class) public void assertSuccessCountFail() {
		new CommandTestHelper(null).assertSuccessCount(CommandResult.empty());
	}

	@Test public void assertSuccessCountPass() {
		new CommandTestHelper(null).assertSuccessCount(CommandResult.success());
	}*/

}
