@file:Suppress("NOTHING_TO_INLINE")
package com.leocth.gravityguns.util.ext

import net.fabricmc.fabric.api.util.NbtType
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtHelper
import net.minecraft.util.Identifier

inline fun NbtCompound.getCompound(key: String, action: (NbtCompound) -> Unit): NbtCompound {
    val compound = getCompound(key)
    action(compound)
    return compound
}

inline fun NbtCompound.putCompound(key: String, init: (NbtCompound) -> Unit) {
    put(key, NbtCompound().apply(init))
}

inline fun NbtCompound.getIdentifier(key: String): Identifier
    = Identifier(getString(key))

inline fun NbtCompound.putIdentifier(key: String, id: Identifier) {
    putString(key, id.toString())
}

inline fun NbtCompound.getItemStack(key: String): ItemStack
    = ItemStack.fromNbt(getCompound(key))

inline fun NbtCompound.putItemStack(key: String, stack: ItemStack)
    = putCompound(key, stack::writeNbt)

inline fun NbtCompound.getBlockState(key: String)
    = NbtHelper.toBlockState(getCompound(key))

inline fun NbtCompound.putBlockState(key: String, state: BlockState) {
    put(key, NbtHelper.fromBlockState(state))
}

inline fun NbtCompound.find(key: String, type: Int, ifFound: (NbtElement) -> Unit): NbtElement? {
    val element = get(key)
    if (element != null && element.type == type.toByte())
        ifFound(element)
    return element
}

inline fun NbtCompound.findCompound(key: String, ifFound: (NbtCompound) -> Unit): NbtCompound? {
    val element = get(key)
    if (element != null && element.type == NbtType.COMPOUND.toByte()) {
        val compound = element as NbtCompound
        ifFound(element)
        return element
    }
    return null
}