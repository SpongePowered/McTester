package org.spongepowered.mctester.internal.framework.proxy

import com.flowpowered.math.vector.Vector3d
import org.spongepowered.api.entity.Entity
import org.spongepowered.mctester.internal.McTester
import org.spongepowered.mctester.internal.RawClient
import org.spongepowered.mctester.internal.ServerOnly
import org.spongepowered.mctester.internal.message.ResponseWrapper
import org.spongepowered.mctester.internal.message.toclient.MessageRPCRequest
import java.util.*

class SuspendRemoteClientProxy(private val proxyCallback: ProxyCallback?) {

    suspend fun lookAt(targetPos: Vector3d) {
        val method = RawClient::class.java.getMethod("lookAt", Vector3d::class.java)
        McTester.INSTANCE.sendToPlayer(MessageRPCRequest(RemoteInvocationData(this, method, arrayListOf(targetPos))))

        getResponse()
    }

    suspend fun lookAt(entity: Entity) {
        val method = RawClient::class.java.getMethod("lookAt", UUID::class.java)
        McTester.INSTANCE.sendToPlayer(MessageRPCRequest(RemoteInvocationData(this, method, arrayListOf(entity.uniqueId))))

        getResponse()
    }


    suspend fun rightClick() {
        val method = RawClient::class.java.getMethod("rightClick")
        McTester.INSTANCE.sendToPlayer(MessageRPCRequest(RemoteInvocationData(this, method, arrayListOf<Any>())))
        getResponse()
    }



    private suspend fun getResponse(): ResponseWrapper {
        val result = ServerOnly.INBOUND_QUEUE.receive()
        this.proxyCallback?.afterInvoke()
        return result
    }

    suspend fun onFullyLoggedIn() {
        val method = RawClient::class.java.getMethod("onFullyLoggedIn")
        McTester.INSTANCE.sendToPlayer(MessageRPCRequest(RemoteInvocationData(this, method, arrayListOf<Any>())))
        val resp = getResponse()
    }

}
