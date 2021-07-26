package com.leocth.gravityguns

import com.leocth.gravityguns.config.GravityGunsConfig
import com.leocth.gravityguns.data.GrabbedBlockPosSelection
import com.leocth.gravityguns.data.GravityGunsTags
import com.leocth.gravityguns.entity.BlockAsAnEntity
import com.leocth.gravityguns.item.GravityGunItem
import com.leocth.gravityguns.network.GravityGunsC2SPackets
import com.leocth.gravityguns.physics.GrabbingManager
import com.leocth.gravityguns.sound.GravityGunsSounds
import dev.lazurite.rayon.core.api.event.collision.ElementCollisionEvents
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

    @JvmField
    val GRAVITY_GUN = GravityGunItem(defaultSettings.maxDamage(2000))

    private val CONFIG_PATH = FabricLoader.getInstance().configDir.resolve("gravityguns.json")
    private val defaultSettings: Item.Settings get() = Item.Settings().group(ITEM_GROUP)
    private val JSON = Json {
        prettyPrint = true
        encodeDefaults = true
    }
    var CONFIG = GravityGunsConfig()
        private set


    override fun onInitialize() {
        updateConfig()
        registerStuff()
        registerEvents()
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun id(path: String) = Identifier(MOD_ID, path)

    private fun updateConfig() {
        if (CONFIG_PATH.exists()) {
            CONFIG = JSON.decodeFromString(Files.readString(CONFIG_PATH))
        }
        saveConfig()
    }

    internal fun saveConfig() {
        Files.writeString(CONFIG_PATH, JSON.encodeToString(CONFIG))
    }

    private fun registerStuff() {
        GravityGunsC2SPackets.registerListeners()
        GravityGunsTags.register()
        GravityGunsSounds.register()

        Registry.register(Registry.ITEM, id("gravity_gun"), GRAVITY_GUN)
        Registry.register(Registry.ENTITY_TYPE, id("block"), BlockAsAnEntity.TYPE)

        TrackedDataHandlerRegistry.register(GrabbedBlockPosSelection.DATA_HANDLER)
    }

    private fun registerEvents() {
        ServerLifecycleEvents.SERVER_STOPPED.register { GrabbingManager.SERVER.instances.clear() }
        ServerTickEvents.END_SERVER_TICK.register { GrabbingManager.SERVER.tick() }

        ElementCollisionEvents.BLOCK_COLLISION.register { element, blockRigidBody, impulse ->
            if (element is BlockAsAnEntity) {
                element.onBlockCollision(blockRigidBody, impulse)
            }
        }
    }
}