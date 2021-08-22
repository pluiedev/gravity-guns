package com.leocth.gravityguns.network

import com.leocth.gravityguns.GravityGuns
import com.leocth.gravityguns.entity.BlockAsAnEntity
import com.leocth.gravityguns.physics.GrabbingManager
import com.leocth.gravityguns.util.ext.PacketByteBuf
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
import net.minecraft.util.math.BlockPos

object GravityGunsS2CPackets {
    val GRAB = GravityGuns.id("grab")
    val UNGRAB = GravityGuns.id("ungrab")
    val MAKE_MESH = GravityGuns.id("make_mesh")

    @Environment(EnvType.CLIENT)
    fun registerListeners() {
        ClientPlayNetworking.registerGlobalReceiver(GRAB) { client, _, buf, _ ->
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
        ClientPlayNetworking.registerGlobalReceiver(UNGRAB) { client, _, buf, _ ->
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
        ClientPlayNetworking.registerGlobalReceiver(MAKE_MESH) { client, _, buf, _ ->
            val entityId = buf.readVarInt()
            val p1 = buf.readBlockPos()
            val p2 = buf.readBlockPos()

            client.execute {
                val world = client.world ?: return@execute

                val entity = world.getEntityById(entityId)
                if (entity is BlockAsAnEntity) {
                    entity.buildMesh(p1, p2)
                }
            }
        }
    }


    fun sendGrabPacket(owner: PlayerEntity, entity: Entity) {
        PlayerLookup.tracking(entity).sendToAll(GRAB, PacketByteBuf {
            it.writeVarInt(owner.id)
            it.writeVarInt(entity.id)
        })
    }

    fun sendUngrabPacket(owner: PlayerEntity, entity: Entity, strength: Float) {
        PlayerLookup.tracking(entity).sendToAll(UNGRAB, PacketByteBuf {
            it.writeVarInt(owner.id)
            it.writeFloat(strength)
        })
    }

    fun sendMakeMeshPacket(owner: PlayerEntity, entity: Entity, p1: BlockPos, p2: BlockPos) {
        if (owner !is ServerPlayerEntity) throw IllegalStateException("this method must be called in the logical server")

        val buf = PacketByteBuf {
            it.writeVarInt(entity.id)
            it.writeBlockPos(p1)
            it.writeBlockPos(p2)
        }
        PlayerLookup.tracking(owner).sendToAll(MAKE_MESH, buf)
        ServerPlayNetworking.send(owner, MAKE_MESH, buf)
    }

    private fun Collection<ServerPlayerEntity>.sendToAll(channelId: Identifier, buf: PacketByteBuf) {
        forEach {
            ServerPlayNetworking.send(it, channelId, buf)
        }
    }
}