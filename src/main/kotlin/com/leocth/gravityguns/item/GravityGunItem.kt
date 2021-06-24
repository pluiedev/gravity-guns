package com.leocth.gravityguns.item

import net.minecraft.block.Blocks
import net.minecraft.entity.EntityType
import net.minecraft.entity.FallingBlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
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
            println(user.rotationVector)
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

                val pos = hit.blockPos
                val fallingEntity = FallingBlockEntity(world, pos.x + 0.5, pos.y + 2.0, pos.z + 0.5, blockState)
                world.spawnEntity(fallingEntity)

                return TypedActionResult.consume(stack)
            }
        }
        return super.use(world, user, hand)
    }

    override fun registerControllers(data: AnimationData) {
        val controller = AnimationController(this, "controller", 20f) { event ->
            //event.controller.setAnimation {
                //it.addAnimation("test")
            //}
            PlayState.CONTINUE
        }

        data.addAnimationController(controller)
    }

    override fun getFactory(): AnimationFactory = factory
}