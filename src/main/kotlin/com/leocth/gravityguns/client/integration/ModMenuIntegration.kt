package com.leocth.gravityguns.client.integration

import com.leocth.gravityguns.GravityGuns
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import me.shedaniel.clothconfiglite.api.ConfigScreen
import net.minecraft.text.TranslatableText
import kotlin.reflect.KMutableProperty0

@Suppress("unused")
object ModMenuIntegration: ModMenuApi {
    override fun getModConfigScreenFactory() = ConfigScreenFactory {
        val config = GravityGuns.CONFIG

        ConfigScreen.create(TranslatableText("screen.gravityguns.config"), it).apply {
            addProp("screen.gravityguns.config.maximumPowerLevel", config::maximumPowerLevel, 5.0)
            addProp("screen.gravityguns.config.launchInitialVelocityMultiplier", config::launchInitialVelocityMultiplier, 20.0)
            addProp("screen.gravityguns.config.entityReachDistance", config::entityReachDistance, 7.0)
            addProp("screen.gravityguns.config.blockReachDistance", config::blockReachDistance, 8.0)
        }.get()
    }

    private inline fun <reified V> ConfigScreen.addProp(translationKey: String, prop: KMutableProperty0<V>, def: V) {
        add(TranslatableText(translationKey), prop.get(), { def }) {
            prop.set(it as? V ?: def)
        }
    }
}