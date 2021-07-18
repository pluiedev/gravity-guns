package com.leocth.gravityguns.entity

import com.glisco.worldmesher.WorldMesh
import com.leocth.gravityguns.data.GrabbedBlockPosSelection
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
import net.minecraft.util.math.Direction
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

    var selection: GrabbedBlockPosSelection
        get() = dataTracker.get(SELECTION)
        private set(value) { dataTracker.set(SELECTION, value) }

    init {
        inanimate = true
    }

    constructor(
        world: World,
        pos: Vec3d,
        selection: GrabbedBlockPosSelection
    ): this(TYPE, world) {
        this.selection = selection
        setPosition(pos.x, pos.y + (1f - height) / 2.0, pos.z)
    }

    override fun genShape(): MinecraftShape = MinecraftShape.of(selection.boundingBox)

    override fun initDataTracker() {
        dataTracker.startTracking(SELECTION, GrabbedBlockPosSelection.EMPTY)
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        selection = GrabbedBlockPosSelection.readFromNbt(nbt)
    }
    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        selection.writeToNbt(nbt)
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
        val down = physPos.offset(Direction.DOWN, selection.ySize)

        val hasSpace =
            !world.getBlockState(down).isAir &&
            world.getFluidState(down).fluid == Fluids.EMPTY

        if (hasSpace) {
            rigidBody.setDoTerrainLoading(false)
            kill()

            selection.forEachEncompassed(physPos) { pos, state ->
                world.breakBlock(pos, true)
                world.setBlockState(pos, state)
            }
        }
    }

    @Environment(EnvType.CLIENT)
    fun buildMesh(p1: BlockPos, p2: BlockPos) {
        // TODO: this is horrible.
        val mesh = WorldMesh.Builder(world, p1, p2).build()

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
        private val SELECTION = DataTracker.registerData(BlockAsAnEntity::class.java, GrabbedBlockPosSelection.DATA_HANDLER)

        val TYPE: EntityType<BlockAsAnEntity> = FabricEntityTypeBuilder.create<BlockAsAnEntity>()
            .dimensions(EntityDimensions.changing(3f, 3f))
            .entityFactory(::BlockAsAnEntity)
            .build()
    }
}