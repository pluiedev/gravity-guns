package com.leocth.gravityguns.data

import com.leocth.gravityguns.GravityGuns
import net.fabricmc.fabric.api.tag.TagRegistry

object GravityGunTags {
    val GRABBABLE_ENTITIES = TagRegistry.entityType(GravityGuns.id("grabbable"))
    val GRABBABLE_BLOCKS = TagRegistry.block(GravityGuns.id("grabbable"))

    fun register() { /* NO-OP */ }
}