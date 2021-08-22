package com.leocth.gravityguns.network

import com.leocth.gravityguns.GravityGuns
import com.leocth.gravityguns.entity.BlockAsAnEntity
import com.leocth.gravityguns.item.GravityGunItem.Companion.power
import com.leocth.gravityguns.physics.GrabbingManager
import com.leocth.gravityguns.util.ext.PacketByteBuf
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.Block
import net.minecraft.network.Packet
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper

object GravityGunsC2SPackets {
    private val UPDATE_POWER = GravityGuns.id("update_power")
    private val MESH_READY = GravityGuns.id("mesh_ready")

    fun registerListeners() {
        ServerPlayNetworking.registerGlobalReceiver(UPDATE_POWER) { server, player, _, buf, _ ->
            val newPower = buf.readDouble()
            server.execute {
                // TODO: support offhand
                val stack = player.mainHandStack

                if (stack.isOf(GravityGuns.GRAVITY_GUN) &&
                    player.isSneaking &&
                    !GrabbingManager.SERVER.isPlayerGrabbing(player)) {

                    stack.power = newPower
                }
            }
        }
        ServerPlayNetworking.registerGlobalReceiver(MESH_READY) { server, player, _, buf, _ ->
            val p1 = buf.readBlockPos()
            val p2 = buf.readBlockPos()
            server.execute {
                BlockPos.iterate(p1, p2).forEach {
                    // TODO: are there faster implementations?
                    player.world.removeBlock(it, false)
                }
            }
        }
    }

    @Environment(EnvType.CLIENT)
    fun sendUpdatePowerPacket(newPower: Double) {
        ClientPlayNetworking.send(UPDATE_POWER, PacketByteBuf {
            it.writeDouble(newPower)
        })
    }

    @Environment(EnvType.CLIENT)
    fun sendMeshReadyPacket(p1: BlockPos, p2: BlockPos) {
        ClientPlayNetworking.send(MESH_READY, PacketByteBuf {
            it.writeBlockPos(p1)
            it.writeBlockPos(p2)
        })
    }
}