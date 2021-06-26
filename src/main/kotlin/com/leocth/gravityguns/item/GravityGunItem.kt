package com.leocth.gravityguns.item

import com.leocth.gravityguns.entity.BlockAsAnEntity
import net.minecraft.block.BlockState
import net.minecraft.entity.FallingBlockEntity
import net.minecraft.entity.LivingEntity
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

    companion object {
        fun getHeldBlock(stack: ItemStack): BlockState
            = NbtHelper.toBlockState(stack.orCreateTag.getCompound("block"))
        fun setHeldBlock(stack: ItemStack, value: BlockState) {
            stack.orCreateTag.put("block", NbtHelper.fromBlockState(value))
        }
        fun hasHeldBlock(stack: ItemStack): Boolean = stack.orCreateTag.contains("block")
        fun removeHeldBlock(stack: ItemStack) { stack.orCreateTag.remove("block") }

        fun isUsing(stack: ItemStack): Boolean = stack.orCreateTag.getBoolean("isUsing")
        fun setUsing(stack: ItemStack, value: Boolean) { stack.orCreateTag.putBoolean("isUsing", value) }
    }

    override fun getMaxUseTime(stack: ItemStack): Int = 20000

    override fun onStoppedUsing(stack: ItemStack, world: World, user: LivingEntity, remainingUseTicks: Int) {
        println("onStoppedUsing")
        if (!world.isClient) {
            /*
            if (hasHeldBlock(stack)) {
                // yeet it
                val state = getHeldBlock(stack)
                val fallingBlockEntity = FallingBlockEntity(world, user.x, user.eyeY - 0.1, user.z, state)
                fallingBlockEntity.timeFalling = 1 // trick MC's verification process
                fallingBlockEntity.velocity = user.rotationVector.multiply(4.0)
                world.spawnEntity(fallingBlockEntity)

                removeHeldBlock(stack)
                if (user is PlayerEntity)
                    user.itemCooldownManager.set(this, 10)

                setUsing(stack, false)
            }

             */
        }
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {

        println("use")
        if (!world.isClient) {
            val stack = user.getStackInHand(hand)
            if (isUsing(stack)) return TypedActionResult.pass(stack)

            val end = user.eyePos.add(user.rotationVector.multiply(8.0))
            val rayCtx = RaycastContext(
                /*start = */ user.eyePos,
                /*end = */ end,
                /*shapeType = */ RaycastContext.ShapeType.COLLIDER,
                /*fluidHandling = */ RaycastContext.FluidHandling.ANY,
                /*entity = */ user
            )
            val hit = world.raycast(rayCtx)

            if (hit.type == HitResult.Type.BLOCK) {
                val blockState = world.getBlockState(hit.blockPos)
                world.removeBlock(hit.blockPos, true)

                val blockPos = hit.blockPos
                val entity = BlockAsAnEntity(world, blockPos.x + 0.5, blockPos.y.toDouble(), blockPos.z + 0.5)
                world.spawnEntity(entity)

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