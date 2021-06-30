package com.leocth.gravityguns.network

import com.leocth.gravityguns.GravityGuns
import com.leocth.gravityguns.physics.GrabbingManager
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

object GravityGunsS2CPackets {
    val GRAB = GravityGuns.id("grab")
    val UNGRAB = GravityGuns.id("ungrab")

    @Environment(EnvType.CLIENT)
    fun registerListeners() {
        ClientPlayNetworking.registerGlobalReceiver(GRAB, this::onGrab)
        ClientPlayNetworking.registerGlobalReceiver(UNGRAB, this::onUngrab)
    }

    @Environment(EnvType.CLIENT)
    fun onGrab(client: MinecraftClient, handler: ClientPlayNetworkHandler, buf: PacketByteBuf, sender: PacketSender) {
        val ownerId = buf.readVarInt()
        val entityId = buf.readVarInt()
        client.execute {
            val world = client.world ?: return@execute

            val owner = world.getEntityById(ownerId)
            val entity = world.getEntityById(entityId)
                ?: throw IllegalStateException("cannot find entity with ID $entityId!")

            if (owner is PlayerEntity) {
                GrabbingManager.CLIENT.tryGrab(owner, entity)
            }
        }
    }

    @Environment(EnvType.CLIENT)
    fun onUngrab(client: MinecraftClient, handler: ClientPlayNetworkHandler, buf: PacketByteBuf, sender: PacketSender) {
        val ownerId = buf.readVarInt()
        val strength = buf.readFloat()
        client.execute {
            val world = client.world ?: return@execute

            val owner = world.getEntityById(ownerId)
            if (owner is PlayerEntity) {
                GrabbingManager.CLIENT.tryUngrab(owner, strength)
            }
        }
    }


    fun sendGrabPacket(owner: PlayerEntity, entity: Entity) {
        PlayerLookup.tracking(entity).sendToAll(GRAB) {
            writeVarInt(owner.id)
            writeVarInt(entity.id)
        }
    }

    fun sendUngrabPacket(owner: PlayerEntity, entity: Entity, strength: Float) {
        PlayerLookup.tracking(entity).sendToAll(UNGRAB) {
            writeVarInt(owner.id)
            writeFloat(strength)
        }
    }

    private inline fun Collection<ServerPlayerEntity>.sendToAll(channelId: Identifier, bufBuilder: PacketByteBuf.() -> Unit) {
        forEach {
            ServerPlayNetworking.send(it, channelId, PacketByteBufs.create().apply(bufBuilder))
        }
    }
}