package org.spongepowered.mctester.internal.framework

import com.flowpowered.math.vector.Vector3d
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.InventoryPlayer
import org.spongepowered.api.data.type.HandType
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.item.inventory.entity.PlayerInventory
import org.spongepowered.mctester.internal.McTester
import org.spongepowered.mctester.internal.RawClient
import org.spongepowered.mctester.internal.framework.proxy.SuspendRemoteClientProxy
import org.spongepowered.mctester.junit.Client
import java.util.*

class ServerSideClientHandler(private val proxyClient: RawClient) : Client {

    private val suspendProxyClient = SuspendRemoteClientProxy(McTester.INSTANCE.syncExecutor, null)



    override fun getClientInventory(): PlayerInventory {
        val itemStacks = this.proxyClient.rawInventory
        val inventory = InventoryPlayer(McTester.getThePlayer() as EntityPlayer)

        for (i in itemStacks.indices) {
            inventory.setInventorySlotContents(i, ItemStack.builder().fromContainer(itemStacks[i]).build() as Any as net.minecraft.item.ItemStack)
        }
        return inventory as PlayerInventory
    }

    override fun getOpenGuiClass(): Optional<String> {
        val className = this.proxyClient.openGuiClass
        return if (className == "") {
            Optional.empty()
        } else Optional.of(className)
    }

    // Boilerplate

    override fun sendMessage(text: String) {
        this.proxyClient.sendMessage(text)
    }

    override fun lookAt(targetPos: Vector3d) {
        this.proxyClient.lookAt(targetPos)
    }

    override suspend fun lookAtSuspend(targetPos: Vector3d) {
        this.suspendProxyClient.lookAt(targetPos)
    }

    override fun lookAt(entity: Entity) {
        this.proxyClient.lookAt(entity.uniqueId)
    }

    override fun selectHotbarSlot(slot: Int) {
        this.proxyClient.selectHotbarSlot(slot)
    }

    override fun leftClick() {
        this.proxyClient.leftClick()
    }

    override fun holdLeftClick(clicking: Boolean) {
        this.proxyClient.holdLeftClick(clicking)
    }

    override fun rightClick() {
        this.proxyClient.rightClick()
    }

    override suspend fun rightClickSuspend() {
        this.suspendProxyClient.rightClick()
    }


    override fun holdRightClick(clicking: Boolean) {
        this.proxyClient.holdRightClick(clicking)
    }

    override fun getItemInHand(type: HandType): ItemStack {
        return this.proxyClient.getItemInHand(type)
    }

    fun onFullyLoggedIn() {
        this.proxyClient.onFullyLoggedIn()
    }
}
