package com.leocth.gravityguns.physics

import com.leocth.gravityguns.data.GravityGunTags
import com.leocth.gravityguns.entity.CompactBlockStates
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.PistonBlock
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import kotlin.math.ceil

interface GrabShape {
    fun compact(
        world: World,
        direction: Direction,
        hitPoint: BlockPos,
        power: Double,
    ): CompactBlockStates
}

object CubeGrabShape: GrabShape {
    override fun compact(
        world: World,
        direction: Direction,
        hitPoint: BlockPos,
        power: Double,
    ): CompactBlockStates {
        val sideLen = ceil(power).toInt()
        val lenMinusOne = sideLen - 1
        val half = lenMinusOne / 2
        val otherHalf = lenMinusOne - half

        val p1 = hitPoint.add(-half,-half,-half)
        val p2 = hitPoint.add(otherHalf, otherHalf, otherHalf)

        val box = BlockBox.create(p1, p2)
        box.move(direction.opposite.vector.multiply(half))

        val states = CompactBlockStates(sideLen, sideLen, sideLen, BlockPos(-half, -half, -half))

        box.forEachEncompassed { x, y, z, pos ->
            val state = world.getBlockState(pos)
            states[x, y, z] = state

            assert(states[x, y, z] == state)
            world.removeBlock(pos, false)
        }

        return states
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
    world.removeBlock(blockPos, false)
    return state
}