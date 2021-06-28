package com.leocth.gravityguns.entity

import com.jme3.math.Vector3f
import com.leocth.gravityguns.util.ext.getBlockState
import com.leocth.gravityguns.util.ext.putBlockState
import dev.lazurite.rayon.core.impl.bullet.collision.body.BlockRigidBody
import dev.lazurite.rayon.core.impl.bullet.collision.space.MinecraftSpace
import dev.lazurite.rayon.core.impl.bullet.thread.PhysicsThread
import dev.lazurite.rayon.entity.api.EntityPhysicsElement
import dev.lazurite.rayon.entity.impl.collision.body.EntityRigidBody
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.Packet
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.world.World

class BlockAsAnEntity(world: World) : Entity(TYPE, world), EntityPhysicsElement {
    var block: BlockState = Blocks.AIR.defaultState
        private set
    private val rigidBody = EntityRigidBody(this)

    init {
        inanimate = true
    }

    constructor(
        world: World,
        x: Double, y: Double, z: Double,
        state: BlockState
    ): this(world) {
        setPosition(x, y + (1f - height) / 2.0, z)
        block = state
    }

    override fun initDataTracker() {}

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        block = nbt.getBlockState("block")
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        nbt.putBlockState("block", block)

    }

    override fun createSpawnPacket(): Packet<*>
        = EntitySpawnS2CPacket(this, Block.getRawIdFromState(block))

    override fun onSpawnPacket(packet: EntitySpawnS2CPacket) {
        super.onSpawnPacket(packet)
        block = Block.getStateFromRawId(packet.entityData)
    }

    override fun step(space: MinecraftSpace) {
        rigidBody.applyCentralForce(Vector3f(0f, 9f, 0f))
    }

    override fun getRigidBody() = rigidBody

    fun onBlockCollision(blockRigidBody: BlockRigidBody, impulse: Float) {
        if (blockY - blockRigidBody.blockPos.y == 1) {
            world.setBlockState(blockPos, block)
            kill()
        }
    }

    companion object {
        val TYPE: EntityType<BlockAsAnEntity> = FabricEntityTypeBuilder.create<BlockAsAnEntity>()
            .dimensions(EntityDimensions.fixed(1f, 1f))
            .entityFactory { _: EntityType<BlockAsAnEntity>, world -> BlockAsAnEntity(world) }
            .build()
    }
}