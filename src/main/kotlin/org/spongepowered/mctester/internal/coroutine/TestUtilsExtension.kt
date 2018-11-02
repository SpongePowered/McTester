package org.spongepowered.mctester.internal.coroutine

import org.spongepowered.api.Sponge
import org.spongepowered.mctester.internal.McTesterDummy
import org.spongepowered.mctester.internal.TickConstants
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CoroutineTestUtils {
    companion object {
        suspend fun waitForInventoryPropagation() {
            waitTicks(TickConstants.INVENTORY_PROPAGATION_TICKS)
        }
    }
}

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
