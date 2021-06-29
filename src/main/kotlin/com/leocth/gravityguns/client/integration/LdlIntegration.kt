package com.leocth.gravityguns.client.integration

import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer

@Suppress("unused")
object LdlIntegration: DynamicLightsInitializer {
    override fun onInitializeDynamicLights() {
        //DynamicLightHandlers.registerDynamicLightHandler(BlockAsAnEntity.TYPE) { it.state.luminance }
    }
}