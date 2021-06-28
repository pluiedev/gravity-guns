package com.leocth.gravityguns.client

import com.leocth.gravityguns.GravityGuns
import com.leocth.gravityguns.client.render.entity.BlockAsAnEntityRenderer
import com.leocth.gravityguns.client.render.item.GravityGunRenderer
import com.leocth.gravityguns.entity.BlockAsAnEntity
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
import net.minecraft.client.world.ClientWorld
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer


@Suppress("UNUSED")
@Environment(EnvType.CLIENT)
object GravityGunsClient: ClientModInitializer {
    override fun onInitializeClient() {
        GravityGunsS2CPackets.registerListeners()

        GeoItemRenderer.registerItemRenderer(GravityGuns.GRAVITY_GUN, GravityGunRenderer())
        EntityRendererRegistry.INSTANCE.register(BlockAsAnEntity.TYPE, ::BlockAsAnEntityRenderer)

        ElementCollisionEvents.BLOCK_COLLISION.register { element, blockRigidBody, impulse ->
            if (element is BlockAsAnEntity) {
                element.onBlockCollision(blockRigidBody, impulse)
            }
        }


        BetterClientLifecycleEvents.DISCONNECT.register { _, _ -> GrabbingManager.CLIENT.instances.clear() }
        ClientTickEvents.END_CLIENT_TICK.register {
            GrabbingManager.CLIENT.tick()
        }
    }
}