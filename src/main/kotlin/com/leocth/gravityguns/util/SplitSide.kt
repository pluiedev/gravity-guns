package com.leocth.gravityguns.util

import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World

data class ServerOnly(
    val player: ServerPlayerEntity,
    val world: ServerWorld,
)
data class ClientOnly(
    val player: ClientPlayerEntity,
    val world: ClientWorld,
)

inline fun splitSide(
    player: PlayerEntity,
    world: World,
    client: (ClientOnly) -> Unit = {},
    server: (ServerOnly) -> Unit = {},
) {
    if (world.isClient) {
        val only = ClientOnly(
            player as ClientPlayerEntity,
            world as ClientWorld
        )
        client(only)
    } else {
        val only = ServerOnly(
            player as ServerPlayerEntity,
            world as ServerWorld
        )
        server(only)
    }
}


inline fun <R> splitSideWithReturn(
    player: PlayerEntity,
    world: World,
    client: (ClientOnly) -> R,
    server: (ServerOnly) -> R,
): R {
    return if (world.isClient) {
        val only = ClientOnly(
            player as ClientPlayerEntity,
            world as ClientWorld
        )
        client(only)
    } else {
        val only = ServerOnly(
            player as ServerPlayerEntity,
            world as ServerWorld
        )
        server(only)
    }
}

