package com.leocth.gravityguns.client

import com.leocth.gravityguns.entity.BlockAsAnEntity
import dev.lambdaurora.lambdynlights.api.DynamicLightHandlers
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer

@Suppress("unused")
object LdlCompat: DynamicLightsInitializer {
    override fun onInitializeDynamicLights() {
        DynamicLightHandlers.registerDynamicLightHandler(BlockAsAnEntity.TYPE) { it.state.luminance }
    }
}