package com.leocth.gravityguns.entity

import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.world.World
import net.snakefangox.worldshell.entity.WorldShellEntity
import net.snakefangox.worldshell.entity.WorldShellSettings

class ClumpEntity(
    type: EntityType<*>,
    world: World,
): WorldShellEntity(type, world, SHELL_SETTINGS) {
    companion object {
        private val SHELL_SETTINGS = WorldShellSettings.Builder(false, true)
            .build()

        val TYPE: EntityType<ClumpEntity> = FabricEntityTypeBuilder.create<ClumpEntity>()
            .dimensions(EntityDimensions.changing(1f, 1f))
            .entityFactory(::ClumpEntity)
            .build()
    }
}