package com.leocth.gravityguns.physics

import com.leocth.gravityguns.util.ext.toBullet
import dev.lazurite.rayon.core.impl.bullet.collision.body.MinecraftRigidBody
import dev.lazurite.rayon.core.impl.bullet.collision.body.shape.MinecraftShape
import dev.lazurite.rayon.core.impl.bullet.collision.space.MinecraftSpace
import net.minecraft.entity.Entity

class WrappedEntityRigidBody(
    val entity: Entity
): MinecraftRigidBody(
    MinecraftSpace.get(entity.world),
    MinecraftShape.of(entity.boundingBox),
    1.0f, 0.05f, 1.0f, 0.5f)
{
    init {
        setPhysicsLocation(entity.pos.toBullet())
    }

    override fun setDoTerrainLoading(doTerrainLoading: Boolean) {}
    override fun shouldDoTerrainLoading(): Boolean = true
}