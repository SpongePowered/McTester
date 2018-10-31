package org.spongepowered.mctester.internal.coroutine

import org.spongepowered.api.Sponge
import org.spongepowered.mctester.internal.McTesterDummy
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


suspend fun waitTicks(ticks: Int) {
    val future = CompletableFuture<Void>()

    Sponge.getScheduler().createTaskBuilder().delayTicks(ticks.toLong()).execute(Runnable { future.complete(null) }).submit(McTesterDummy.INSTANCE)
    future.await()
    println("Done!")
}


// Copied from https://github.com/Kotlin/kotlin-coroutines/blob/e818483161742280d0ea300fbc666620b2dbacde/examples/future/await.kt
suspend fun <T> CompletableFuture<T>.await(): T =
        suspendCoroutine { cont: Continuation<T> ->
            whenComplete { result, exception ->
                if (exception == null) // the future has been completed normally
                    cont.resume(result)
                else // the future has completed with an exception
                    cont.resumeWithException(exception)
            }
        }
