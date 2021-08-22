package com.leocth.gravityguns.util.ext

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.network.PacketByteBuf

inline fun PacketByteBuf(builder: (PacketByteBuf) -> Unit): PacketByteBuf
    = PacketByteBufs.create().also(builder)