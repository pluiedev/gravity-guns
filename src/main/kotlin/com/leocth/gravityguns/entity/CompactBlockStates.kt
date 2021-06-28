package com.leocth.gravityguns.entity

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.data.TrackedDataHandler
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.BlockPos

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

    constructor(
        length: Int,
        width: Int,
        height: Int,
        offset: BlockPos,
        vararg blockStates: BlockState
    ): this(length, width, height, offset,
        IntArray(length * width * height) { Block.getRawIdFromState(blockStates[it]) }
    )

    init {
        require(length * width * height == size)
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
                    val state = this[x, y, z]
                    action(x, y, z, pos, state)
                    pos.y++
                }
                pos.z++
            }
            pos.x++
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