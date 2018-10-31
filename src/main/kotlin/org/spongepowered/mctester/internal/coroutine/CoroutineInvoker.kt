package org.spongepowered.mctester.internal.coroutine

import kotlinx.coroutines.CoroutineScope
import org.junit.runners.model.FrameworkMethod
import org.spongepowered.mctester.internal.InvokeMethodWrapper
import org.spongepowered.mctester.internal.InvokerCallback
import org.spongepowered.mctester.internal.framework.TesterManager
import java.lang.reflect.InvocationTargetException
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

class CoroutineInvoker(testMethod: FrameworkMethod, target: Any, callback: InvokerCallback, private val testerManager: TesterManager) : InvokeMethodWrapper(testMethod, target, callback) {
    private var coroutineTestManager = CoroutineTestManager(this.getMethodAsSuspendBlock(this.target), this.testerManager)

    override fun doInvocation() {
        this.coroutineTestManager.runToCompletion()
    }

    private fun getMethodAsSuspendBlock(testClassInstance: Any): suspend CoroutineScope.() -> Unit {
        val callable = this.method.method.declaringClass.kotlin.members.first {
            val function: KFunction<*> = it as KFunction<*>
            function.javaMethod == this.method.method
        }

        return {
            suspendCoroutine<Unit> {
                try {
                    callable.call(testClassInstance, it)
                } catch (e: InvocationTargetException) {
                    throw e.cause ?: e
                }
            }
        }
    }
}
