package com.leocth.gravityguns.physics

import com.leocth.gravityguns.data.GrabbedBlockPosSelection
import com.leocth.gravityguns.data.GravityGunsTags
import net.minecraft.block.BlockState
import net.minecraft.block.PistonBlock
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

abstract class GrabShape {
    abstract fun grab(
        user: PlayerEntity,
        world: World,
        direction: Direction,
        hitPoint: BlockPos,
        hitState: BlockState,
        power: Double,
    ): GrabbedBlockPosSelection?

    // filtering blocks that cannot be grabbed:
    // 1) it cannot allow mobs to spawn inside, i.e. air & cave air
    // 2) it cannot house block entities, since block entity logic is tricky (although TODO I can add this later...?)
    // 3) it cannot be denied by the deny list
    protected fun isBlockImmobile(world: World, pos: BlockPos, state: BlockState): Boolean =
        state.isAir ||
        world.getBlockEntity(pos) != null ||
        state.block in GravityGunsTags.IMMOBILE_BLOCKS ||
        (
            state.block is PistonBlock &&
            state.get(PistonBlock.EXTENDED) // disallow extended pistons
        )
}

object CubeGrabShape : GrabShape() {
    override fun grab(
        user: PlayerEntity,
        world: World,
        direction: Direction,
        hitPoint: BlockPos,
        hitState: BlockState,
        power: Double
    ): GrabbedBlockPosSelection? {
        if (isBlockImmobile(world, hitPoint, hitState)) return null

        val half = power.toInt() - 1

        val off = direction.opposite.vector.multiply(half)
        val min = hitPoint.mutableCopy().move(-half, -half, -half).move(off)
        val max = hitPoint.mutableCopy().move(half, half, half).move(off)

        //TODO: handle edge cases like double blocks, ground foliage, etc
        return GrabbedBlockPosSelection.createAndPopulate(min, max, BlockPos.ORIGIN) {
            val state = world.getBlockState(it)
            if (isBlockImmobile(world, it, state)) {
                if (state.isAir)
                    null
                else
                    return null // cancel the selection since an invalid block is included
            } else {
                state
            }
        }
    }
}

