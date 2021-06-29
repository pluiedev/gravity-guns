package com.leocth.gravityguns

import com.leocth.gravityguns.config.GravityGunsConfig
import com.leocth.gravityguns.data.GravityGunTags
import com.leocth.gravityguns.entity.BlockAsAnEntity
import com.leocth.gravityguns.entity.CompactBlockStates
import com.leocth.gravityguns.item.GravityGunItem
import com.leocth.gravityguns.network.GravityGunsC2SPackets
import com.leocth.gravityguns.physics.GrabbingManager
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import java.nio.file.Files
import kotlin.io.path.exists

@Suppress("UNUSED")
object GravityGuns: ModInitializer {
    const val MOD_ID = "gravityguns"

    val ITEM_GROUP: ItemGroup = FabricItemGroupBuilder.build(id("group")) { ItemStack(GRAVITY_GUN) }
    val GRAVITY_GUN = GravityGunItem(defaultSettings.maxDamage(2000))
    var CONFIG = GravityGunsConfig()
        private set

    private val defaultSettings: Item.Settings get() = Item.Settings().group(ITEM_GROUP)
    private val JSON = Json {
        prettyPrint = true
        encodeDefaults = true
    }


    override fun onInitialize() {
        updateConfig()
        registerStuff()
        registerEvents()
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun id(path: String) = Identifier(MOD_ID, path)

    private fun updateConfig() {
        val path = FabricLoader.getInstance().configDir.resolve("gravityguns.json")
        if (path.exists()) {
            CONFIG = JSON.decodeFromString(Files.readString(path))
        }
        Files.writeString(path, JSON.encodeToString(CONFIG))
    }

    private fun registerStuff() {
        GravityGunsC2SPackets.registerListeners()
        GravityGunTags.register()

        Registry.register(Registry.ITEM, id("gravity_gun"), GRAVITY_GUN)
        Registry.register(Registry.ENTITY_TYPE, id("block"), BlockAsAnEntity.TYPE)

        TrackedDataHandlerRegistry.register(CompactBlockStates.DATA_HANDLER)
    }

    private fun registerEvents() {
        ServerLifecycleEvents.SERVER_STOPPED.register { GrabbingManager.SERVER.instances.clear() }
        ServerTickEvents.END_SERVER_TICK.register { GrabbingManager.SERVER.tick() }
    }
}