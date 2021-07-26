package com.leocth.gravityguns.client

import com.leocth.gravityguns.GravityGuns
import net.minecraft.client.render.entity.model.BipedEntityModel
import net.minecraft.entity.LivingEntity
import net.minecraft.util.Arm
import net.minecraft.util.Hand

object BipedEntityModelDelegate {
    @JvmStatic
    fun <T: LivingEntity> positionRightArm(entity: T, model: BipedEntityModel<T>) {
        val hand = if (entity.mainArm == Arm.RIGHT) Hand.MAIN_HAND else Hand.OFF_HAND
        if (entity.getStackInHand(hand).isOf(GravityGuns.GRAVITY_GUN)) {
            model.rightArm.pitch = toRad(270f)
            model.leftArm.pitch = toRad(280f)
            model.leftArm.yaw = toRad(7f)
        }
    }

    @JvmStatic
    fun <T: LivingEntity> positionLeftArm(entity: T, model: BipedEntityModel<T>) {
        val hand = if (entity.mainArm == Arm.LEFT) Hand.MAIN_HAND else Hand.OFF_HAND
        if (entity.getStackInHand(hand).isOf(GravityGuns.GRAVITY_GUN)) {
            model.leftArm.pitch = toRad(270f)
            model.rightArm.pitch = toRad(280f)
            model.rightArm.yaw = toRad(7f)

        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun toRad(deg: Float) = (deg / 180f * Math.PI).toFloat()
}