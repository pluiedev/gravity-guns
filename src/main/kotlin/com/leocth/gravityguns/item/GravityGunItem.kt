package com.leocth.gravityguns.item

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtHelper
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.hit.HitResult
import net.minecraft.world.RaycastContext
import net.minecraft.world.World
import software.bernie.geckolib3.core.IAnimatable
import software.bernie.geckolib3.core.PlayState
import software.bernie.geckolib3.core.controller.AnimationController
import software.bernie.geckolib3.core.manager.AnimationData
import software.bernie.geckolib3.core.manager.AnimationFactory

class GravityGunItem(settings: Settings) : Item(settings), IAnimatable {
    private val factory = AnimationFactory(this)

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        if (!world.isClient) {
            val stack = user.getStackInHand(hand)

            val rayCtx = RaycastContext(
                /*start = */ user.eyePos,
                /*end = */ user.rotationVector.multiply(20.0),
                /*shapeType = */ RaycastContext.ShapeType.COLLIDER,
                /*fluidHandling = */ RaycastContext.FluidHandling.ANY,
                /*entity = */ user
            )
            val hit = world.raycast(rayCtx)

            if (hit.type == HitResult.Type.BLOCK) {
                val blockState = world.getBlockState(hit.blockPos)

                val tag = stack.orCreateTag
                tag.put("block", NbtHelper.fromBlockState(blockState))

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