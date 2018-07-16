package org.spongepowered.mctester.junit;

import com.flowpowered.math.vector.Vector3d;
import kotlin.coroutines.experimental.Continuation;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.text.Text;

import java.util.Optional;

/**
 * The Client controller.
 *
 * <p>This interface is used to directly control the Minecraft
 * client. None of these methods use normal SpongeAPI methods.
 * Instead, they direct the client to perform a particular action.
 *
 * For example, {@link #sendMessage(String)} causes a chat GUI to open
 * on the actual client, the specified message to be inputted,
 * and finally sent to the server.
 */
interface Client {

    /**
     * Causes the client to send a chat message
     *
     * <p>This method performs the following client-side actions:
     *    - Opening the chat GUI
     *    - Entering the specified message
     *    - Pressing 'Enter' to send the message to the server, closing the chat GUI in the process
     *
     *    Accordingly, it will cause a {@link MessageChannelEvent.Chat} to be fired.
     *
     * Unlike {@link Player#simulateChat(Text, Cause)}, this method actuallly
     * causes a chat packet to be sent to the server.
     * @param text The message to send
     */
    fun sendMessage(text: String);

    /**
     * Causes the client to look at the specified block.
     *
     * <p>This is equivalent to the user moving their mouse
     * to look at the targeted block. Accordingly, it will cause
     * {@link MoveEntityEvent}s to be fired.</p>
     * @param targetPos The position to look at
     */
    fun lookAt(targetPos: Vector3d);

    suspend fun lookAtSuspend(targetPos: Vector3d);

    /**
     * Causes the client to look at the specified entity.
     *
     * <p>This is equivalent to the user moving their mouse
     * to look at the targeted entity. Accordingly, it will cause
     * {@link MoveEntityEvent}s to be fired.</p>
     * @param entity The entity to look at
     */
    fun lookAt(entity: Entity);

    /**
     * Causes the client to select the given hotbar slot.
     *
     * <p>This is equivalent to the player pressing a hotkey
     * on their keyboard (usually a number key) to select
     * the given hotbar slot.</p>
     * @param slot The slot to select
     */
    fun selectHotbarSlot(slot: Int);

    /**
     * Causes the client to perform a left mouse click.
     *
     * <p>The result of this action is dependent on what the client
     * is currently looking at, as well as what it is currently holding
     * - just like a human pressing the left mouse button.</p>
     *
     * <p>This method performs an instantaneously click - it only lasts
     * for one client tick. To perform a longer click, use
     * {@link #holdLeftClick(boolean)}.</p>
     */
    fun leftClick();

    /**
     * Causes the client to start or stop holding a left click.
     *
     * <p>If {@param clicking} is <code>true</code>, the client
     * will hold down left click until {@link #holdLeftClick(boolean)}
     * is called with <code>false</code>.</p>
     *
     * @param clicking The clicking state
     */
    fun holdLeftClick(clicking: Boolean);

    /**
     * Causes the client to perform a right mouse click.
     *
     * <p>The result of this action is dependent on what the client
     * is currently looking at, as well as what it is currently holding
     * - just like a human pressing the right mouse button.</p>
     *
     * <p>This method performs an instantaneously click - it only lasts
     * for one client tick. To perform a longer click, use
     * {@link #holdRightClick(boolean)}.</p>
     *
     */
    fun rightClick();

    suspend fun rightClickSuspend();

    /**
     * Causes the client to start or stop holding a right click.
     *
     * <p>If {@param clicking} is <code>true</code>, the client
     * will hold down right click until {@link #holdRightClick(boolean)}
     * is called with <code>false</code>.</p>
     *
     * @param clicking The clicking state
     */
    fun holdRightClick(clicking: Boolean);

    /**
     * Gets the item in the client's specified hand.
     *
     * <p>Unlike {@link Player#getItemInHand(HandType)},
     * this method returns the actual contents of the client-side
     * inventory, not the server-side representation of it.
     *
     * This method can be useful for verifying that a
     * server-side inventory change actually updates the client.</p>
     *
     * @param type The {@link HandType} to get the {@link ItemStack} for
     * @return the {@link ItemStack} in the specified {@link HandType}
     */
    fun getItemInHand(type: HandType): ItemStack ;

    /**
     * Returns a copy of the player's client-side inventory.
     *
     * <p>Unlike {@link Player#getInventory()}, this inventory
     * represents the actual state of the player's client-side inventory,
     * not the server-side representation of it.
     *
     * Modifying the returned {@link PlayerInventory} will affect
     * neither the client nor the server. Effectively, it is a snapshot
     * of the player's client-side inventory at the time it was called.</p>
     * @return
     */
    fun getClientInventory(): PlayerInventory ;

    fun getOpenGuiClass(): Optional<String>;

}
