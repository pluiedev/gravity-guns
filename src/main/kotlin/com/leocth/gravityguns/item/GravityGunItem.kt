package com.leocth.gravityguns.item

import com.leocth.gravityguns.physics.GrabUtil
import com.leocth.gravityguns.physics.GrabbingManager
import com.leocth.gravityguns.util.ext.toBullet
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World
import software.bernie.geckolib3.core.IAnimatable
import software.bernie.geckolib3.core.PlayState
import software.bernie.geckolib3.core.controller.AnimationController
import software.bernie.geckolib3.core.manager.AnimationData
import software.bernie.geckolib3.core.manager.AnimationFactory

class GravityGunItem(settings: Settings) : Item(settings), IAnimatable {
    private val factory = AnimationFactory(this)

    companion object {
        var ItemStack.power: Double
            get() = tag?.getDouble("power") ?: 0.0
            set(value) { orCreateTag.putDouble("power", value) }
    }

    override fun getMaxUseTime(stack: ItemStack): Int = 20000

    override fun onStoppedUsing(stack: ItemStack, world: World, user: LivingEntity, remainingUseTicks: Int) {
        if (!world.isClient) {
            val grabbingManager = GrabbingManager.SERVER

            if (user is ServerPlayerEntity && grabbingManager.isPlayerGrabbing(user)) {
                val instance = grabbingManager.instances[user.uuid]
                instance?.let {
                    val impulse = user.rotationVector.multiply(20.0).toBullet()
                    it.grabbedBody.setLinearVelocity(impulse)
                }

                grabbingManager.tryUngrab(user, 5.0f)
            }
        }
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        if (!world.isClient) {
            val stack = user.getStackInHand(hand)

            val grabbingManager = GrabbingManager.SERVER

            if (!grabbingManager.isPlayerGrabbing(user)) {
                val thingToGrab =
                    GrabUtil.getEntityToGrab(user, 7.0) ?:
                    GrabUtil.getBlockToGrab(user, 8.0, stack.power) ?:
                    return TypedActionResult.fail(stack)

                grabbingManager.tryGrab(user, thingToGrab)
                user.setCurrentHand(hand)
                return TypedActionResult.consume(stack)
            }
        }
        return super.use(world, user, hand)
    }

    override fun registerControllers(data: AnimationData) {
        val controller = AnimationController(this, "controller", 5f) { event ->
            //event.controller.setAnimation {
                //it.addAnimation("test")
            //}
            PlayState.CONTINUE
        }

        data.addAnimationController(controller)
    }

    override fun getFactory(): AnimationFactory = factory
}