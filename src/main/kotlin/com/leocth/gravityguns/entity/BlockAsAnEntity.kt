package com.leocth.gravityguns.entity

import com.glisco.worldmesher.WorldMesh
import com.leocth.gravityguns.data.CompactBlockStates
import com.leocth.gravityguns.network.GravityGunsC2SPackets
import com.leocth.gravityguns.physics.GrabbingManager
import com.leocth.gravityguns.util.ext.*
import dev.lazurite.rayon.core.impl.bullet.collision.body.BlockRigidBody
import dev.lazurite.rayon.core.impl.bullet.collision.body.shape.MinecraftShape
import dev.lazurite.rayon.core.impl.bullet.collision.space.MinecraftSpace
import dev.lazurite.rayon.entity.api.EntityPhysicsElement
import dev.lazurite.rayon.entity.impl.collision.body.EntityRigidBody
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.data.DataTracker
import net.minecraft.fluid.Fluids
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.Packet
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class BlockAsAnEntity(
    entityType: EntityType<*>,
    world: World,
) : Entity(entityType, world), EntityPhysicsElement {

    @Environment(EnvType.CLIENT)
    var mesh: WorldMesh? = null
        private set

    @get:JvmName("wtfIsThis")
    private val rigidBody by lazy { EntityRigidBody(this) }

    var states: CompactBlockStates
        get() = dataTracker.get(BLOCK_STATES)
        private set(value) { dataTracker.set(BLOCK_STATES, value) }

    init {
        inanimate = true
    }

    constructor(
        world: World,
        pos: Vec3d,
        states: CompactBlockStates
    ): this(TYPE, world) {
        this.states = states
        setPosition(pos.x, pos.y + (1f - height) / 2.0, pos.z)
    }

    override fun genShape(): MinecraftShape = MinecraftShape.of(states.boundingBox)

    override fun initDataTracker() {
        dataTracker.startTracking(BLOCK_STATES, CompactBlockStates.makeEmpty())
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        states.readFromNbt(nbt)
    }
    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        states.writeToNbt(nbt)
    }

    override fun createSpawnPacket(): Packet<*>
        = EntitySpawnS2CPacket(this)

    override fun step(space: MinecraftSpace) {
        //rigidBody.applyCentralForce(Vector3f(0f, 9f, 0f))
    }

    override fun getRigidBody() = rigidBody

    fun onBlockCollision(blockRigidBody: BlockRigidBody, impulse: Float) {
        if (world.isClient || isRemoved) return
        if (GrabbingManager.SERVER.isEntityBeingGrabbed(this)) return // don't settle if it's still being grabbed

        val physPos = BlockPos(rigidBody.getPhysicsLocation(null).toVec3d())
        val down = physPos.down()

        val hasSpace =
            !world.getBlockState(down).isAir &&
            world.getFluidState(down).fluid == Fluids.EMPTY

        if (hasSpace) {
            rigidBody.setDoTerrainLoading(false)
            kill()

            val mutablePhysPos = physPos.mutableCopy()
            val states = this.states
            states.forEach { _, _, _, pos, state ->
                if (state.isAir) return@forEach

                mutablePhysPos.move(pos)
                world.breakBlock(mutablePhysPos, true)
                world.setBlockState(mutablePhysPos, state)
                mutablePhysPos.set(physPos)
            }


        }
    }

    @Environment(EnvType.CLIENT)
    fun buildMesh(p1: BlockPos, p2: BlockPos) {
        // TODO: this is horrible.
        val mesh = WorldMesh.Builder(world, p1, p2).build() ?: throw IllegalStateException("what the fuck?")

        while (!mesh.canRender()) {
            // g l i s c o w h y ?
            runBlocking {
                delay(50)
            }
        }
        this.mesh = mesh
        GravityGunsC2SPackets.sendMeshReadyPacket(p1, p2)
    }

    companion object {
        private val BLOCK_STATES = DataTracker.registerData(BlockAsAnEntity::class.java, CompactBlockStates.DATA_HANDLER)

        val TYPE: EntityType<BlockAsAnEntity> = FabricEntityTypeBuilder.create<BlockAsAnEntity>()
            .dimensions(EntityDimensions.fixed(3f, 3f))
            .entityFactory(::BlockAsAnEntity)
            .build()
    }
}