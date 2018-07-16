package org.spongepowered.mctester.junit

/**
 * Indicagtes that the annotation test method should be run as a
 * coroutine on the main thread.
 *
 * Coroutine tests MUST be written in Kotlin - barring drastic changes to Java,
 * it will never be possible to write them in Java.
 */
annotation class CoroutineTest;
