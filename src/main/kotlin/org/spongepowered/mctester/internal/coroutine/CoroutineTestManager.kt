package org.spongepowered.mctester.internal.coroutine

import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Runnable
import kotlinx.coroutines.experimental.runBlocking
import org.spongepowered.api.Sponge
import org.spongepowered.mctester.internal.framework.TesterManager
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.CoroutineContext

class CoroutineTestManager(val block: suspend CoroutineScope.() -> Unit, val testerManager: TesterManager) {

    var continuation: Continuation<Any>? = null

    fun runToCompletion() {
        runBlocking(context = MainThreadDispatcher(this.testerManager), block = this.block)
    }
}

private class MainThreadDispatcher(val testManager: TesterManager): CoroutineDispatcher() {

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        try {
            if (Sponge.getServer().isMainThread) {
                block.run()
            } else {
                this.testManager.runOnMainThread(block)
            }
        } catch (e: Throwable) {
            e.printStackTrace();
            throw RuntimeException("Exception when scheduling coroutine block on main thread!", e)
        }
    }

}
