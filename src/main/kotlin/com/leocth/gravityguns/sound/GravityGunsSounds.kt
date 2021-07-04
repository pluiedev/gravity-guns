package com.leocth.gravityguns.sound

import com.leocth.gravityguns.GravityGuns
import net.minecraft.sound.SoundEvent
import net.minecraft.util.registry.Registry

object GravityGunsSounds {
    fun register() {
        registerSounds(
            "item.gravity_gun.shutdown",
            "item.gravity_gun.retract",
            "item.gravity_gun.extend",
            "item.gravity_gun.beam",
            "item.gravity_gun.woo",
        )
    }

    private fun registerSounds(vararg ids: String) {
        for (id in ids) {
            val i = GravityGuns.id(id)
            Registry.register(Registry.SOUND_EVENT, i, SoundEvent(i))
        }
    }
}