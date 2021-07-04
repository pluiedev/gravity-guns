package com.leocth.gravityguns.data

import com.leocth.gravityguns.util.ext.findIntArray
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.data.TrackedDataHandler
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box

class CompactBlockStates(
    length: Int,
    width: Int,
    height: Int,
    offset: BlockPos,

    private var blockStates: IntArray = IntArray(length * width * height)
) {
    var length: Int = length
        private set
    var width: Int = width
        private set
    var height: Int = height
        private set
    var offset: BlockPos = offset
        private set

    val size: Int get() = blockStates.size
    val boundingBox: Box get() = Box(offset, offset.add(length, height, width))

    init {
        require(length * width * height == size)
    }

    fun writeToNbt(nbt: NbtCompound) {
        with(nbt) {
            putIntArray("dimensions", intArrayOf(length, width, height))
            putIntArray("offset", intArrayOf(offset.x, offset.y, offset.z))
            putIntArray("states", blockStates)
        }
    }

    fun readFromNbt(nbt: NbtCompound) {
        with(nbt) {
            findIntArray("dimensions") {
                require(it.size == 3)
                length = it[0]
                width = it[1]
                height = it[2]
            }
            findIntArray("offset") {
                require(it.size == 3)
                val (x, y, z) = it
                offset = BlockPos(x, y, z)
            }
            blockStates = getIntArray("states")
        }
    }

    fun writeToBuf(buf: PacketByteBuf) {
        with(buf) {
            writeVarInt(length)
            writeVarInt(width)
            writeVarInt(height)
            writeBlockPos(offset)
            writeIntArray(blockStates)
        }
    }

    fun readFromBuf(buf: PacketByteBuf) {
        with(buf) {
            length = readVarInt()
            width = readVarInt()
            height = readVarInt()
            offset = readBlockPos()
            blockStates = readIntArray(length * width * height)
        }
    }

    inline fun forEach(action: (x: Int, y: Int, z: Int, pos: BlockPos, state: BlockState) -> Unit) {
        val pos = offset.mutableCopy()
        for (x in 0 until length) {
            for (z in 0 until width) {
                for (y in 0 until height) {
                    pos.move(x, y, z)
                    val state = this[x, y, z]
                    action(x, y, z, pos, state)
                    pos.set(offset)
                }
            }
        }
    }

    fun indexAt(x: Int, y: Int, z: Int): Int
        = x + z * length + y * length * width

    operator fun get(x: Int, y: Int, z: Int): BlockState
        = Block.getStateFromRawId(blockStates[indexAt(x, y, z)])
    operator fun set(x: Int, y: Int, z: Int, state: BlockState) {
        blockStates[indexAt(x, y, z)] = Block.getRawIdFromState(state)
    }

    companion object {
        fun makeEmpty() = CompactBlockStates(0, 0, 0, BlockPos.ORIGIN, intArrayOf())

        val DATA_HANDLER = object : TrackedDataHandler<CompactBlockStates> {
            override fun write(buf: PacketByteBuf, value: CompactBlockStates) {
                value.writeToBuf(buf)
            }

            override fun read(buf: PacketByteBuf): CompactBlockStates {
                val states = makeEmpty()
                states.readFromBuf(buf)
                return states
            }

            override fun copy(value: CompactBlockStates): CompactBlockStates
                = CompactBlockStates(value.length, value.width, value.height, BlockPos(value.offset), value.blockStates.copyOf())
        }
    }
}