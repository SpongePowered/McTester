# McTester

An integration testing framework for Minecraft

## What is McTester?

McTester allows you to write fully automated integration tests against Minecraft.
Through an easy-to-use API, you can direct a real single-player client to perform actions in the world (sending commands, looking around, clicking the mouse).
Combined with a server-side API like Sponge, McTester allows you to write automated tests that simply wouldn't be possible otherwise.

## Why not just write unit tests, and avoid the overhead of starting a full Minecraft client? 

McTester is designed to complement a project's existing tests. Here are a few examples of things you can do with it:

* Spawn in and ignite a creeper, verifying that the an explosion event is triggered within a fixed number of ticks.
* Test that using an in-game item (such as eating an apple) causes the proper events to be fired.
* Verify that a command block GUI always opens on the client when the player has been granted permission by a permissions plugin.

Without McTester, the only way to perform these kinds of tests is by manually running the game.  
With McTester, you can run tens or hundreds of tests as part of your normal build process. You no longer have to manually test that a difficult-to-reproduce bug hasn't regressed.

## How does McTester work?

McTester is implemented as a Junit `@Runner`. Behind the scenes, it manages three different threads:

* The main thread, where JUnit discovers and invokes tests
* The server thread, where most of your test code will be running.
* The client thread, where an internal Sponge plugin executes action requets from the McTester server plugin.

[ExceptionTest](https://github.com/Aaron1011/McTester/blob/9573c60d87c41c1868ca5f2003ad20323a99ccd0/src/test/java/org/spongepowered/mctester/ExceptionTest.java) provides a simple example of a fully-functional McTester test.

## Kotlin and coroutines

MCTester provides experimental support for writing tests using Kotlin coroutines.
This allows your test to be run directly on the main thread, without needing to constantly wrap blocks of code in`TestUtils.runOnMainThread`.

When your test performs an action that would normally be blocking - e.g. waiting for the client to perform a right click - your test method is suspended.
Once the client response reaches the server, your method is automatically scheduled back onto the main thread.

For a test writer, all of this is completely transparent. If you annotate your `suspend fun` with `@CoroutineTest`,
McTester will allow you to write ordinary Kotlin code that runs on the main server thread, without freezing the game.

-------

Inspired by https://github.com/vorburger/SwissKnightMinecraft/tree/master/SpongePowered/SpongeTests
