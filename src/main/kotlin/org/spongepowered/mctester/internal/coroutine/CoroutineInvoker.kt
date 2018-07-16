package org.spongepowered.mctester.internal.coroutine

import kotlinx.coroutines.experimental.CoroutineScope
import org.junit.runners.model.FrameworkMethod
import org.spongepowered.mctester.internal.InvokeMethodWrapper
import org.spongepowered.mctester.internal.InvokerCallback
import org.spongepowered.mctester.internal.framework.TesterManager
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.suspendCoroutine
import kotlin.reflect.KCallable
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

class CoroutineInvoker(testMethod: FrameworkMethod, target: Any, callback: InvokerCallback, val testerManager: TesterManager) : InvokeMethodWrapper(testMethod, target, callback) {
    private var coroutineTestManager = CoroutineTestManager(this.getMethodAsSuspendBlock(this.target), this.testerManager)

    override fun doInvocation() {
        this.coroutineTestManager.runToCompletion()
    }

    private fun getMethodAsSuspendBlock(testClassInstance: Any): suspend CoroutineScope.() -> Unit {
        val callable = this.method.method.declaringClass.kotlin.members.first {
            val function: KFunction<*> = it as KFunction<*>
            function.javaMethod == this.method.method
        }

        val wrapper: suspend CoroutineScope.() -> Unit = {
            suspendCoroutine<Unit> {
                callable.call(testClassInstance, it)
            }
        }

        return wrapper
    }
}
