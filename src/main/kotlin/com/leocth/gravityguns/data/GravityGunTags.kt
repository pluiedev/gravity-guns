package com.leocth.gravityguns.data

import com.leocth.gravityguns.GravityGuns
import net.fabricmc.fabric.api.tag.TagRegistry
import net.minecraft.block.Block
import net.minecraft.entity.EntityType
import net.minecraft.tag.Tag

object GravityGunTags {
    val IMMOBILE_ENTITIES: Tag<EntityType<*>> = TagRegistry.entityType(GravityGuns.id("immobile"))
    val IMMOBILE_BLOCKS: Tag<Block> = TagRegistry.block(GravityGuns.id("immobile"))

    fun register() { /* NO-OP */ }
}