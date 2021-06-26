package com.leocth.gravityguns.client

import com.leocth.gravityguns.GravityGuns
import com.leocth.gravityguns.client.render.GravityGunRenderer
import net.fabricmc.api.ClientModInitializer
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer

@Suppress("UNUSED")
object GravityGunsClient: ClientModInitializer {
    override fun onInitializeClient() {
        GeoItemRenderer.registerItemRenderer(GravityGuns.GRAVITY_GUN, GravityGunRenderer())
    }
}