package com.leocth.gravityguns.client.integration

import com.leocth.gravityguns.GravityGuns
import com.leocth.gravityguns.config.GravityGunsConfig
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import kotlinx.serialization.encodeToString
import me.shedaniel.clothconfiglite.api.ConfigScreen
import net.minecraft.text.TranslatableText
import java.nio.file.Files
import kotlin.reflect.KMutableProperty0

@Suppress("unused")
object ModMenuIntegration: ModMenuApi {
    override fun getModConfigScreenFactory() = ConfigScreenFactory {
        val config = GravityGuns.CONFIG
        val def = GravityGunsConfig()

        ConfigScreen.create(TranslatableText("screen.gravityguns.config"), it).apply {
            addProp("screen.gravityguns.config.maximumPowerLevel", config::maximumPowerLevel, def.maximumPowerLevel)
            addProp("screen.gravityguns.config.launchInitialVelocityMultiplier", config::launchInitialVelocityMultiplier, def.launchInitialVelocityMultiplier)
            addProp("screen.gravityguns.config.entityReachDistance", config::entityReachDistance, def.entityReachDistance)
            addProp("screen.gravityguns.config.blockReachDistance", config::blockReachDistance, def.blockReachDistance)
        }.get()
    }

    private inline fun <reified V> ConfigScreen.addProp(translationKey: String, prop: KMutableProperty0<V>, def: V) {
        add(TranslatableText(translationKey), prop.get(), { def }) {
            prop.set(it as? V ?: def)
            GravityGuns.saveConfig()
        }
    }
}