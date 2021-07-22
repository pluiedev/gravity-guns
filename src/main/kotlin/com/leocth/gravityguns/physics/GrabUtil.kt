package com.leocth.gravityguns.physics

import com.leocth.gravityguns.data.GravityGunsTags
import com.leocth.gravityguns.entity.BlockAsAnEntity
import com.leocth.gravityguns.network.GravityGunsS2CPackets
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d

// Shamelessly stolen from Thinking with Portatos
object GrabUtil {
    fun getEntityToGrab(user: PlayerEntity, reach: Double): Entity? {
        val cameraPos = user.getCameraPosVec(1f)
        val rotationVec = user.getRotationVec(1f)
        val extended = rotationVec.multiply(reach)

        val end = cameraPos.add(extended)
        val box = user.boundingBox.stretch(extended).expand(1.0)

        val result = raycastEntity(user, cameraPos, end, box, reach) {
            it.type !in GravityGunsTags.IMMOBILE_ENTITIES
        } ?: return null

        return result.entity?.let {
            if (it.hasVehicle()) it.vehicle else it
        }
    }

    fun getBlockToGrab(user: PlayerEntity, reach: Double, power: Double): BlockAsAnEntity? {
        val result = user.raycast(reach, 1f, false)
        val world = user.world

        if (result.type != HitResult.Type.MISS && result is BlockHitResult) {
            val grabShape = CubeGrabShape // TODO
            val blockPos = result.blockPos
            val state = world.getBlockState(blockPos)

            val sel = grabShape.grab(user, world, result.side, blockPos, state, power) ?: return null

            val bEntity = BlockAsAnEntity(
                world,
                Vec3d.ofBottomCenter(blockPos),
                sel
            )
            world.spawnEntity(bEntity)
            // TODO: this is concern
            // TODO: also worldmesher disregards the filter, might need to tackle this later
            GravityGunsS2CPackets.sendMakeMeshPacket(user, bEntity, sel.min, sel.max)

            return bEntity
        }
        return null
    }

    /* NOTE:
        This is taken directly from Thinking with Portatos.
        However, judging by the code itself, it's more likely to be another copy of a Minecraft-internal method,
        since decompiler artifacts (such as the iterator raw loop and nondescript locals) are present.
        I think I should be allowed to use this.
     */
    private fun raycastEntity(
        self: Entity,
        start: Vec3d,
        end: Vec3d,
        box: Box,
        range: Double,
        predicate: (Entity) -> Boolean,
    ): EntityHitResult? {
        val world = self.world
        var r = range
        var entity: Entity? = null
        var hit: Vec3d? = null

        for (e in world.getOtherEntities(self, box, predicate)) {
            // targetingMargin sounds like stuff used in path finding, although I only care about its effect
            val bigBox = e.boundingBox.expand(e.targetingMargin.toDouble())
            val optional = bigBox.raycast(start, end)

            if (bigBox.contains(start)) {
                // if our head is right inside the entity
                if (r >= 0.0) {
                    entity = e
                    hit = optional.orElse(start)
                    r = 0.0 // there's no point in going further
                }
            } else if (optional.isPresent) {
                // our ray is inside the box
                val successfulHit = optional.get()
                val squaredDistance = start.squaredDistanceTo(successfulHit)
                if (squaredDistance < r || r == 0.0) {
                    // distance's in range; we can reach it
                    if (e.rootVehicle === self.rootVehicle) {
                        if (r == 0.0) {
                            entity = e
                            hit = successfulHit
                        }
                    } else {
                        entity = e
                        hit = successfulHit
                        r = squaredDistance // restrict the range even more
                    }
                }
            }
        }

        if (entity == null) return null
        return EntityHitResult(entity, hit)
    }


}