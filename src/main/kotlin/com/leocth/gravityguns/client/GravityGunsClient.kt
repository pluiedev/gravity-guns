package com.leocth.gravityguns.client

import com.leocth.gravityguns.GravityGuns
import com.leocth.gravityguns.client.render.entity.BlockAsAnEntityRenderer
import com.leocth.gravityguns.client.render.item.GravityGunRenderer
import com.leocth.gravityguns.entity.BlockAsAnEntity
import com.leocth.gravityguns.network.GravityGunsS2CPackets
import dev.lazurite.rayon.core.api.PhysicsElement
import dev.lazurite.rayon.core.api.event.collision.ElementCollisionEvents
import dev.lazurite.rayon.core.impl.bullet.collision.body.BlockRigidBody
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry
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
    }
}