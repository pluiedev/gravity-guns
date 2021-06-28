package com.leocth.gravityguns

import com.leocth.gravityguns.data.GravityGunTags
import com.leocth.gravityguns.entity.BlockAsAnEntity
import com.leocth.gravityguns.item.GravityGunItem
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

@Suppress("UNUSED")
object GravityGuns: ModInitializer {
    const val MOD_ID = "gravityguns"

    val ITEM_GROUP: ItemGroup = FabricItemGroupBuilder.build(id("group")) { ItemStack(GRAVITY_GUN) }
    val GRAVITY_GUN = GravityGunItem(defaultSettings.maxDamage(2000))

    override fun onInitialize() {
        GravityGunTags.register()

        Registry.register(Registry.ITEM, id("gravity_gun"), GRAVITY_GUN)
        Registry.register(Registry.ENTITY_TYPE, id("block"), BlockAsAnEntity.TYPE)
    }

    val defaultSettings get() = Item.Settings().group(ITEM_GROUP)

    @Suppress("NOTHING_TO_INLINE")
    inline fun id(path: String) = Identifier(MOD_ID, path)
}