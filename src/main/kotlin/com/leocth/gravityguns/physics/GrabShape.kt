package com.leocth.gravityguns.physics

import com.glisco.worldmesher.WorldMesh
import com.glisco.worldmesher.internals.WorldMesher
import com.leocth.gravityguns.data.GravityGunTags
import com.leocth.gravityguns.data.CompactBlockStates
import com.leocth.gravityguns.network.GravityGunsS2CPackets
import kotlinx.coroutines.runBlocking
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.BlockState
import net.minecraft.block.PistonBlock
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import kotlin.math.ceil

interface GrabShape {
    fun compact(
        user: PlayerEntity,
        world: World,
        direction: Direction,
        hitPoint: BlockPos,
        power: Double,
    ): Triple<CompactBlockStates, BlockPos, BlockPos>

    @Environment(EnvType.CLIENT)
    fun makeMesh(
        world: ClientWorld,
        p1: BlockPos,
        p2: BlockPos,
    ): WorldMesh
}

object CubeGrabShape: GrabShape {
    override fun compact(
        user: PlayerEntity,
        world: World,
        direction: Direction,
        hitPoint: BlockPos,
        power: Double,
    ): Triple<CompactBlockStates, BlockPos, BlockPos> {
        val sideLen = ceil(power).toInt()
        val lenMinusOne = sideLen - 1
        val half = lenMinusOne / 2
        val otherHalf = lenMinusOne - half

        val off = direction.opposite.vector.multiply(half)
        val p1 = hitPoint.add(-half,-half,-half).add(off)
        val p2 = hitPoint.add(otherHalf, otherHalf, otherHalf).add(off)

        val box = BlockBox.create(p1, p2)

        val states = CompactBlockStates(sideLen, sideLen, sideLen, BlockPos(-half, -half, -half))

        box.forEachEncompassed { x, y, z, pos ->
            removeAndGetStateAt(world, pos)?.let {
                states[x, y, z] = it
            }
        }

        return Triple(states, p1, p2)
    }

    override fun makeMesh(world: ClientWorld, p1: BlockPos, p2: BlockPos): WorldMesh {
        TODO("Not yet implemented")
    }
}

inline fun BlockBox.forEachEncompassed(action: (Int, Int, Int, BlockPos) -> Unit) {
    val origin = BlockPos(minX, minY, minZ)
    val mutable = origin.mutableCopy()
    for (x in 0 until blockCountX) {
        for (y in 0 until blockCountY) {
            for (z in 0 until blockCountZ) {
                mutable.move(x, y, z)
                action(x, y, z, mutable)
                mutable.set(origin)
            }
        }
    }
}

// filtering blocks that cannot be grabbed:
// 1) it cannot allow mobs to spawn inside, i.e. air & cave air
// 2) it cannot house block entities, since block entity logic is tricky (although TODO I can add this later...?)
// 3) it cannot be denied by the deny list

// TODO: allow moving multiple blocks: could be useful when moving double blocks such as doors, or moving multiple blocks as a cluster
private fun isBlockImmobile(world: World, pos: BlockPos, state: BlockState): Boolean
        = state.isAir ||
        world.getBlockEntity(pos) != null ||
        state.block in GravityGunTags.IMMOBILE_BLOCKS ||
        (
            state.block is PistonBlock &&
            state.get(PistonBlock.EXTENDED) // disallow extended pistons
        )

private fun removeAndGetStateAt(world: World, blockPos: BlockPos): BlockState? {
    val state = world.getBlockState(blockPos)
    if (isBlockImmobile(world, blockPos, state)) return null
    //world.removeBlock(blockPos, false)
    return state
}