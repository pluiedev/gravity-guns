package com.leocth.gravityguns.entity

import com.leocth.gravityguns.util.ext.getBlockState
import com.leocth.gravityguns.util.ext.putBlockState
import dev.lazurite.rayon.core.impl.physics.space.MinecraftSpace
import dev.lazurite.rayon.core.impl.physics.space.body.ElementRigidBody
import dev.lazurite.rayon.entity.api.EntityPhysicsElement
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.Packet
import net.minecraft.world.World

class BlockAsAnEntity(world: World) : Entity(TYPE, world), EntityPhysicsElement {
    var block: BlockState = Blocks.AIR.defaultState
        private set
    private val rigidBody = ElementRigidBody(this)

    init {
        inanimate = true
    }

    constructor(world: World, x: Double, y: Double, z: Double): this(world) {
        setPosition(x, y + (1f - height) / 2.0, z)
    }

    override fun initDataTracker() {}

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        block = nbt.getBlockState("block")
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        nbt.putBlockState("block", block)

    }

    override fun createSpawnPacket(): Packet<*> {
        TODO("Not yet implemented")
    }

    override fun step(space: MinecraftSpace) {
    }

    override fun getRigidBody() = rigidBody

    companion object {
        val TYPE = FabricEntityTypeBuilder.create<BlockAsAnEntity>()
            .dimensions(EntityDimensions.fixed(1f, 1f))
            .build()
    }
}