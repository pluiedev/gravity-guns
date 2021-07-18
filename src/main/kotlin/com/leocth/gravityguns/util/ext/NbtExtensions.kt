@file:Suppress("NOTHING_TO_INLINE")
package com.leocth.gravityguns.util.ext

import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos

inline fun NbtCompound.getBlockPos(key: String): BlockPos
    = getLong(key).let {
        BlockPos.fromLong(it)
    }

inline fun NbtCompound.putBlockPos(key: String, pos: BlockPos)
    = putLong(key, pos.asLong())
