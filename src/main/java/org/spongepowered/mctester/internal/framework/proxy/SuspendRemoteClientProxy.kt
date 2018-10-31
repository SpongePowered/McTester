package org.spongepowered.mctester.internal.framework.proxy

import com.flowpowered.math.vector.Vector3d
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.spongepowered.api.entity.Entity
import kotlinx.coroutines.launch;
import org.spongepowered.api.scheduler.SpongeExecutorService
import org.spongepowered.mctester.internal.McTester
import org.spongepowered.mctester.internal.RawClient
import org.spongepowered.mctester.internal.ServerOnly
import org.spongepowered.mctester.internal.message.ResponseWrapper
import org.spongepowered.mctester.internal.message.toclient.MessageRPCRequest
import java.util.*

class SuspendRemoteClientProxy(val mainThreadExecutor: SpongeExecutorService, val proxyCallback: ProxyCallback?) {

    suspend fun lookAt(targetPos: Vector3d) {
        val method = RawClient::class.java.getMethod("lookAt", Vector3d::class.java)
        McTester.INSTANCE.sendToPlayer(MessageRPCRequest(RemoteInvocationData(this, method, arrayListOf(targetPos))))

        val resp = getResponse()
    }

    suspend fun lookAt(entity: Entity) {
        val method = RawClient::class.java.getMethod("lookAt", UUID::class.java)
        McTester.INSTANCE.sendToPlayer(MessageRPCRequest(RemoteInvocationData(this, method, arrayListOf(entity.uniqueId))))

        val resp = getResponse()
    }


    suspend fun rightClick() {
        val method = RawClient::class.java.getMethod("rightClick")
        McTester.INSTANCE.sendToPlayer(MessageRPCRequest(RemoteInvocationData(this, method, arrayListOf<Any>())))
        val resp = getResponse()
    }



    private suspend fun getResponse(): ResponseWrapper {
        val result = coroutineScope {
            ServerOnly.INBOUND_QUEUE
            async(block =  {
                ServerOnly.INBOUND_QUEUE.take()
            }).await()
        }


        this.proxyCallback?.afterInvoke()
        return result
    }

    suspend fun onFullyLoggedIn() {
        val method = RawClient::class.java.getMethod("onFullyLoggedIn")
        McTester.INSTANCE.sendToPlayer(MessageRPCRequest(RemoteInvocationData(this, method, arrayListOf<Any>())))
        val resp = getResponse()
    }

}
