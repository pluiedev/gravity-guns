package com.leocth.gravityguns.physics

import com.glisco.worldmesher.WorldMesh
import com.leocth.gravityguns.data.GravityGunsTags
import com.leocth.gravityguns.data.CompactBlockStates
import com.leocth.gravityguns.mixin.SimpleVoxelShapeMixin
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.BlockState
import net.minecraft.block.PistonBlock
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.BitSetVoxelSet
import net.minecraft.util.shape.SimpleVoxelShape
import net.minecraft.util.shape.VoxelShapes
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
}

object CubeGrabShape: GrabShape {
    override fun compact(
        user: PlayerEntity,
        world: World,
        direction: Direction,
        hitPoint: BlockPos,
        power: Double,
    ): Triple<CompactBlockStates, BlockPos, BlockPos> {
        /*
        val half = power.toInt()

        val off = direction.opposite.vector.multiply(half)
        val p1 = hitPoint.add(-half,-half,-half).add(off)
        val p2 = hitPoint.add(half, half, half).add(off)

        val box = BlockBox.create(p1, p2)

        val sl = half * 2 + 1
        val states = CompactBlockStates(sl, sl, sl, BlockPos.ORIGIN)

        box.forEachEncompassed { x, y, z, pos ->
            removeAndGetStateAt(world, pos)?.let {
                states[x, y, z] = it
            }
        }

        return Triple(states, p1, p2)

         */
        val half = power.toInt()

        val off = direction.opposite.vector.multiply(half)
        val p1 = hitPoint.add(-half,-half,-half).add(off)
        val p2 = hitPoint.add(half, half, half).add(off)

        val box = BlockBox.create(p1, p2)
        val sl = half * 2 + 1

        val set = BitSetVoxelSet(sl, sl, sl)

        box.forEachEncompassed { x, y, z, pos ->
            val state = world.getBlockState(pos)
            if (!isBlockImmobile(world, pos, state)) {
                //TODO: is this slow?
                println("state=$state, x=$x,y=$y,z=$z")
                set.set(x, y, z)
            }
        }

        val shape = SimpleVoxelShapeMixin.create(set)

        val compare = VoxelShapes.combine(
            VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 1.0, 1.0),
            VoxelShapes.cuboid(0.0, 0.0, 0.0, sl.toDouble(), sl.toDouble(), sl.toDouble()),
            BooleanBiFunction.AND
        )

        println(shape)
        println(compare)
        TODO()
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
        state.block in GravityGunsTags.IMMOBILE_BLOCKS ||
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