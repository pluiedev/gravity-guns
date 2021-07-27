package com.leocth.gravityguns.data

import com.leocth.gravityguns.GravityGuns
import net.fabricmc.fabric.api.tag.TagRegistry
import net.minecraft.block.Block
import net.minecraft.entity.EntityType
import net.minecraft.tag.Tag

object GravityGunsTags {
    val IMMOBILE_ENTITIES: Tag<EntityType<*>> = TagRegistry.entityType(GravityGuns.id("immobile"))
    val IMMOBILE_BLOCKS: Tag<Block> = TagRegistry.block(GravityGuns.id("immobile"))

    // Basically these act as 'allow lists', and overrides the 'deny lists' above whenever they are present.
    val MOBILE_ENTITIES: Tag<EntityType<*>> = TagRegistry.entityType(GravityGuns.id("mobile"))
    val MOBILE_BLOCKS: Tag<Block> = TagRegistry.block(GravityGuns.id("mobile"))

    fun register() { /* NO-OP */ }

    fun isImmobile(block: Block) =
        if (MOBILE_BLOCKS.values().isNotEmpty())
            block !in MOBILE_BLOCKS
        else
            block in IMMOBILE_BLOCKS

    fun isImmobile(entityType: EntityType<*>) =
        if (MOBILE_ENTITIES.values().isNotEmpty())
            entityType !in MOBILE_ENTITIES
        else
            entityType in IMMOBILE_ENTITIES
}