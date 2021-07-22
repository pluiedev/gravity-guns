package com.leocth.gravityguns.client

import com.leocth.gravityguns.GravityGuns
import com.leocth.gravityguns.client.render.entity.BlockAsAnEntityRenderer
import com.leocth.gravityguns.client.render.item.GravityGunRenderer
import com.leocth.gravityguns.entity.BlockAsAnEntity
import com.leocth.gravityguns.item.GravityGunItem.Companion.power
import com.leocth.gravityguns.network.GravityGunsC2SPackets
import com.leocth.gravityguns.network.GravityGunsS2CPackets
import com.leocth.gravityguns.physics.GrabbingManager
import dev.lazurite.rayon.core.api.event.collision.ElementCollisionEvents
import dev.lazurite.rayon.core.impl.util.event.BetterClientLifecycleEvents
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import net.minecraft.util.math.MathHelper
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer

@Suppress("UNUSED")
@Environment(EnvType.CLIENT)
object GravityGunsClient: ClientModInitializer {
    override fun onInitializeClient() {
        GravityGunsS2CPackets.registerListeners()

        GeoItemRenderer.registerItemRenderer(GravityGuns.GRAVITY_GUN, GravityGunRenderer())
        EntityRendererRegistry.INSTANCE.register(BlockAsAnEntity.TYPE, ::BlockAsAnEntityRenderer)

        BetterClientLifecycleEvents.DISCONNECT.register { _, _ -> GrabbingManager.CLIENT.instances.clear() }
        ClientTickEvents.END_CLIENT_TICK.register {
            GrabbingManager.CLIENT.tick()
        }
    }

    fun onScroll(client: MinecraftClient, ci: CallbackInfo, delta: Float) {
        val player = client.player ?: return
        // TODO: support offhand
        val stack = player.mainHandStack

        if (stack.isOf(GravityGuns.GRAVITY_GUN) &&
            player.isSneaking &&
            !GrabbingManager.CLIENT.isPlayerGrabbing(player)) {

            val clampedDelta = MathHelper.clamp(delta, -1f, 1f)

            var power = stack.power
            power += clampedDelta
            power = MathHelper.clamp(power, 0.0, GravityGuns.CONFIG.maximumPowerLevel)

            GravityGunsC2SPackets.sendUpdatePowerPacket(power)
            player.sendMessage(TranslatableText("text.gravityguns.update_power", power), true)

            stack.power = power
            ci.cancel()
        }
    }
}