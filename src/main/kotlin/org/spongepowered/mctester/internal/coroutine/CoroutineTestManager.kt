package org.spongepowered.mctester.internal.coroutine

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.spongepowered.api.Sponge
import org.spongepowered.mctester.internal.framework.TesterManager
import kotlin.coroutines.CoroutineContext

class CoroutineTestManager(private val block: suspend CoroutineScope.() -> Unit, private val testerManager: TesterManager) {

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
            e.printStackTrace()
            throw RuntimeException("Exception when scheduling coroutine block on main thread!", e)
        }
    }

}
