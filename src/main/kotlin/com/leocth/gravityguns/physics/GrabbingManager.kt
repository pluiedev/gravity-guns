package com.leocth.gravityguns.physics

import com.leocth.gravityguns.GravityGuns
import com.leocth.gravityguns.network.GravityGunsS2CPackets
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Inspired by/taken directly from `BodyGrabbingManager` of Thinking with Portatos,
 * as I can't for the life of me figure it out on my own.
 */
class GrabbingManager(val isServer: Boolean) {
    val instances = ConcurrentHashMap<UUID, Instance>()

    fun tick() {
        forEachInstance { instance ->
            val (owner) = instance

            if (isServer && owner.itemsHand.none { it.isOf(GravityGuns.GRAVITY_GUN) }) {
                tryUngrab(owner, 0.0f)
            }
        }
    }

    fun tryGrab(owner: PlayerEntity, entity: Entity) {
        if (isPlayerGrabbing(owner) || isEntityBeingGrabbed(entity)) return

        if (owner is ServerPlayerEntity) {
            GravityGunsS2CPackets.sendGrabPacket(owner, entity)
        }

        instances[owner.uuid] = Instance(
            owner, entity,
        )
    }

    fun tryUngrab(owner: PlayerEntity, strength: Float) {
        instances.remove(owner.uuid)
    }

    fun isPlayerGrabbing(player: PlayerEntity) = instances.containsKey(player.uuid)
    fun isEntityBeingGrabbed(entity: Entity) = instances.any { it.value.entity == entity }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun forEachInstance(noinline action: (Instance) -> Unit) {
        instances.forEachValue(PARALLELISM_THRESHOLD, action)
    }

    companion object {
        private const val PARALLELISM_THRESHOLD = 4L

        val SERVER = GrabbingManager(true)
        val CLIENT = GrabbingManager(false)

        fun get(isServer: Boolean): GrabbingManager
            = if (isServer) SERVER else CLIENT
    }

    data class Instance(
        val owner: PlayerEntity,
        val entity: Entity,
    )
}
