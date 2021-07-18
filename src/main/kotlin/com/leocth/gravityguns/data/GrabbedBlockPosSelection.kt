package com.leocth.gravityguns.data

import com.leocth.gravityguns.util.ext.getBlockPos
import com.leocth.gravityguns.util.ext.putBlockPos
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.data.TrackedDataHandler
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import java.util.*

class GrabbedBlockPosSelection(
    val min: BlockPos,
    val max: BlockPos,
    val selectionOffset: BlockPos,
    populateFunc: (BlockPos) -> BlockState? = { null }
) {
    val xSize = max.x - min.x + 1
    val ySize = max.y - min.y + 1
    val zSize = max.z - min.z + 1

    internal var states = IntArray(xSize * ySize * zSize)

    init {
        val pos = min.mutableCopy()
        var i = 0
        for (y in 0 until ySize) {
            for (z in 0 until zSize) {
                for (x in 0 until xSize) {
                    pos.move(x, y, z)
                    states[i] = Block.getRawIdFromState(populateFunc(pos))
                    pos.set(min)
                    i++
                }
            }
        }
    }

    val apparentDisplayOffset: Vec3d by lazy {
        Vec3d(
            xSize / 2.0 + selectionOffset.x,
            ySize / 2.0 + selectionOffset.y,
            zSize / 2.0 + selectionOffset.z
        )
    }

    val boundingBox: Box by lazy { Box(min, max.add(1, 1, 1)) }

    fun writeToNbt(nbt: NbtCompound) {
        with(nbt) {
            putBlockPos("min", min)
            putBlockPos("max", max)
            putIntArray("states", states)
            putBlockPos("off", selectionOffset)
        }
    }

    fun writeToBuf(buf: PacketByteBuf) {
        with(buf) {
            writeBlockPos(min)
            writeBlockPos(max)
            writeIntArray(states)
            writeBlockPos(selectionOffset)
        }
    }

    fun forEachEncompassed(basis: BlockPos = min, action: (pos: BlockPos, state: BlockState) -> Unit) {
        val pos = basis.mutableCopy()
        var i = 0
        for (y in 0 until ySize) {
            for (z in 0 until zSize) {
                for (x in 0 until xSize) {
                    pos.move(x, y, z)
                    val state = states[i]
                    if (state != -1)
                        action(pos, Block.getStateFromRawId(state))
                    pos.set(basis)
                    i++
                }
            }
        }
    }

    companion object {
        val DATA_HANDLER = object : TrackedDataHandler<GrabbedBlockPosSelection> {
            override fun write(buf: PacketByteBuf, value: GrabbedBlockPosSelection) {
                value.writeToBuf(buf)
            }

            override fun read(buf: PacketByteBuf): GrabbedBlockPosSelection {
                return readFromBuf(buf)
            }

            override fun copy(value: GrabbedBlockPosSelection): GrabbedBlockPosSelection =
                GrabbedBlockPosSelection(value.min, value.max, value.selectionOffset).apply {
                    states = value.states.clone()
                }
        }

        val EMPTY = GrabbedBlockPosSelection(BlockPos.ORIGIN, BlockPos.ORIGIN, BlockPos.ORIGIN)

        fun readFromNbt(nbt: NbtCompound): GrabbedBlockPosSelection {
            val min = nbt.getBlockPos("min")
            val max = nbt.getBlockPos("max")
            val states = nbt.getIntArray("set")
            val off = nbt.getBlockPos("off")
            return GrabbedBlockPosSelection(min, max, off).apply {
                this.states = states
            }
        }

        fun readFromBuf(buf: PacketByteBuf): GrabbedBlockPosSelection {
            val min = buf.readBlockPos()
            val max = buf.readBlockPos()
            val states = buf.readIntArray()
            val off = buf.readBlockPos()
            return GrabbedBlockPosSelection(min, max, off).apply {
                this.states = states
            }
        }
    }
}