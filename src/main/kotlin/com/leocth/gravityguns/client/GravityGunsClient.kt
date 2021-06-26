package com.leocth.gravityguns.client

import com.leocth.gravityguns.GravityGuns
import com.leocth.gravityguns.client.render.entity.BlockAsAnEntityRenderer
import com.leocth.gravityguns.client.render.item.GravityGunRenderer
import com.leocth.gravityguns.entity.BlockAsAnEntity
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer

@Suppress("UNUSED")
@Environment(EnvType.CLIENT)
object GravityGunsClient: ClientModInitializer {
    override fun onInitializeClient() {
        GeoItemRenderer.registerItemRenderer(GravityGuns.GRAVITY_GUN, GravityGunRenderer())
        EntityRendererRegistry.INSTANCE.register(BlockAsAnEntity.TYPE, ::BlockAsAnEntityRenderer)
    }
}