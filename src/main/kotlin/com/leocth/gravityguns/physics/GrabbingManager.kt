package com.leocth.gravityguns.physics

import com.jme3.bullet.collision.shapes.EmptyShape
import com.jme3.bullet.joints.SixDofSpringJoint
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.math.Matrix3f
import com.jme3.math.Quaternion
import com.jme3.math.Vector3f
import com.leocth.gravityguns.GravityGuns
import com.leocth.gravityguns.network.GravityGunsS2CPackets
import com.leocth.gravityguns.util.ext.toBullet
import com.leocth.gravityguns.util.ext.toVec3d
import dev.lazurite.rayon.core.api.event.collision.PhysicsSpaceEvents
import dev.lazurite.rayon.core.impl.bullet.collision.space.MinecraftSpace
import dev.lazurite.rayon.entity.api.EntityPhysicsElement
import dev.lazurite.rayon.entity.impl.collision.body.EntityRigidBody
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

/**
 * Inspired by/taken directly from `BodyGrabbingManager` of Thinking with Portatos,
 * as I can't for the life of me figure it out on my own.
 */
class GrabbingManager(val isServer: Boolean) {
    val instances = ConcurrentHashMap<UUID, Instance>()

    init {
        PhysicsSpaceEvents.STEP.register(this::step)
    }

    fun step(space: MinecraftSpace) {
        // update grab point's location, to be in front of the player
        forEachInstance {
            it.grabbedBody.activate()

            it.grabPoint.setPhysicsLocation(getTargetPos(it.owner))
        }
    }

    private fun getTargetPos(owner: PlayerEntity): Vector3f
        = owner.getCameraPosVec(1f)
            .add(owner.rotationVector.multiply(3.5))
            .toBullet()

    fun tick() {
        forEachInstance { instance ->
            val (owner, entity, grabbedBody) = instance

            if (isServer && owner.itemsHand.none { it.isOf(GravityGuns.GRAVITY_GUN) }) {
                tryUngrab(owner, 0.0f)
            }


            if (grabbedBody is EntityRigidBody) {
                val location = grabbedBody.getPhysicsLocation(null)
                val yCenter = entity.boundingBox.yLength / 2.0
                entity.updatePosition(
                    location.x.toDouble(),
                    location.y - yCenter,
                    location.z.toDouble()
                )
            }
        }
    }

    fun tryGrab(owner: PlayerEntity, entity: Entity) {
        if (isPlayerGrabbing(owner) || isEntityBeingGrabbed(entity)) return

        if (owner is ServerPlayerEntity) {
            GravityGunsS2CPackets.sendGrabPacket(owner, entity)
        }

        val space = MinecraftSpace.get(owner.world)

        val grabbedBody = if (entity is EntityPhysicsElement) {
            entity.rigidBody.also { it.activate() }
        } else {
            WrappedEntityRigidBody(entity)
        }

        val targetPos = getTargetPos(owner)

        // Wut?
        val holdBody = PhysicsRigidBody(EMPTY_SHAPE, 0f)
        holdBody.setPhysicsLocation(targetPos)

        val joint = SixDofSpringJoint(
            grabbedBody,
            holdBody,
            Vector3f.ZERO,
            Vector3f.ZERO,
            Matrix3f.IDENTITY,
            Matrix3f.IDENTITY,
            false
        ).apply {
            setLinearLowerLimit(Vector3f.ZERO)
            setLinearUpperLimit(Vector3f.ZERO)
            setAngularLowerLimit(Vector3f.ZERO)
            setAngularUpperLimit(Vector3f.ZERO)
        }

        space.workerThread.execute {
            if (entity !is EntityPhysicsElement) {
                space.addCollisionObject(grabbedBody)
            }
            space.addCollisionObject(holdBody)
            space.addJoint(joint)
        }

        instances[owner.uuid] = Instance(
            owner, entity,
            grabbedBody, joint, holdBody
        )
    }

    fun tryUngrab(owner: PlayerEntity, strength: Float) {
        val instance = instances.remove(owner.uuid) ?: return

        val (_, entity, grabbedBody, joint, point) = instance

        val unit = owner.rotationVector
        if (owner is ServerPlayerEntity) {
            GravityGunsS2CPackets.sendUngrabPacket(owner, entity, strength)

            if (grabbedBody is WrappedEntityRigidBody) {
                val velocity = grabbedBody.getLinearVelocity(null).multLocal(0.05f).toVec3d()
                entity.addVelocity(velocity.x, velocity.y, velocity.z)
                entity.addVelocity(
                    unit.x * strength * 0.05f,
                    unit.y * strength * 0.05f,
                    unit.z * strength * 0.05f
                )
            }
        }
        val config = GravityGuns.CONFIG
        val velocity = unit.multiply(config.launchInitialVelocityMultiplier).toBullet()

        val space = MinecraftSpace.get(owner.world)
        space.workerThread.execute {
            grabbedBody.setLinearVelocity(velocity)
            if (strength > 0f) {
                grabbedBody.setAngularVelocity(Vector3f(
                    Random.nextFloat() * 4 - 2,
                    Random.nextFloat() * 4 - 2,
                    Random.nextFloat() * 4 - 2,
                ))
            }
            if (grabbedBody is WrappedEntityRigidBody) {
                space.removeCollisionObject(grabbedBody)
            }
            space.removeCollisionObject(point)
            space.removeJoint(joint)
        }
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

        val EMPTY_SHAPE by lazy { EmptyShape(false) }

        fun get(isServer: Boolean): GrabbingManager
            = if (isServer) SERVER else CLIENT
    }

    data class Instance(
        val owner: PlayerEntity,
        val entity: Entity,
        val grabbedBody: PhysicsRigidBody,

        val grabJoint: SixDofSpringJoint,
        val grabPoint: PhysicsRigidBody
    )
}
