package com.leocth.gravityguns.client

import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer

@Suppress("unused")
object LdlCompat: DynamicLightsInitializer {
    override fun onInitializeDynamicLights() {
        //DynamicLightHandlers.registerDynamicLightHandler(BlockAsAnEntity.TYPE) { it.state.luminance }
    }
}