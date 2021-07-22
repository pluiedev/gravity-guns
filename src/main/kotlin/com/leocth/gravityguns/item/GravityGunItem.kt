package com.leocth.gravityguns.item

import com.leocth.gravityguns.GravityGuns
import com.leocth.gravityguns.config.GravityGunsConfig
import com.leocth.gravityguns.physics.GrabUtil
import com.leocth.gravityguns.physics.GrabbingManager
import com.leocth.gravityguns.util.ext.setAnimation
import com.leocth.gravityguns.util.splitSide
import com.leocth.gravityguns.util.splitSideWithReturn
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World
import software.bernie.geckolib3.core.AnimationState
import software.bernie.geckolib3.core.IAnimatable
import software.bernie.geckolib3.core.PlayState
import software.bernie.geckolib3.core.controller.AnimationController
import software.bernie.geckolib3.core.event.SoundKeyframeEvent
import software.bernie.geckolib3.core.manager.AnimationData
import software.bernie.geckolib3.core.manager.AnimationFactory
import software.bernie.geckolib3.network.GeckoLibNetwork
import software.bernie.geckolib3.network.ISyncable
import software.bernie.geckolib3.util.GeckoLibUtil

class GravityGunItem(settings: Settings) : Item(settings), IAnimatable, ISyncable {
    private val factory = AnimationFactory(this)

    init {
        GeckoLibNetwork.registerSyncable(this)
    }

    companion object {
        private const val CONTROLLER_ID = "controller"
        private const val EXTEND = 0
        private const val RETRACT = 1

        var ItemStack.power: Double
            get() = tag?.getDouble("power") ?: 0.0
            set(value) {
                orCreateTag.putDouble("power", value)
            }
    }

    override fun getMaxUseTime(stack: ItemStack): Int = 20000

    override fun onStoppedUsing(stack: ItemStack, world: World, user: LivingEntity, remainingUseTicks: Int) {
        if (user !is PlayerEntity) return

        return splitSide(user, world,
            server = { only ->
                val grabbingManager = GrabbingManager.SERVER
                val player = only.player

                if (grabbingManager.isPlayerGrabbing(player)) {
                    val instance = grabbingManager.instances[player.uuid]
                    instance?.let {
                        val config = GravityGuns.CONFIG
                        val velocity = player.rotationVector.multiply(config.launchInitialVelocityMultiplier)//.toBullet()
                        //PhysicsThread.get(world).execute {
                            //it.grabbedBody.setLinearVelocity(velocity)
                        //}
                    }

                    grabbingManager.tryUngrab(player, 5.0f)
                    player.itemCooldownManager.set(GravityGuns.GRAVITY_GUN, 15)
                    syncAnimation(player, world, stack, RETRACT)
                }
            }
        )
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> = splitSideWithReturn(user, world,
        server = {
            val stack = user.getStackInHand(hand)
            val grabbingManager = GrabbingManager.SERVER
            val power = stack.power

            if (power <= 0f) TypedActionResult.fail(stack)
            if (grabbingManager.isPlayerGrabbing(user)) TypedActionResult.fail(stack)

            if (attemptToGrab(grabbingManager, it.player, it.world, GravityGuns.CONFIG, power)) {
                user.setCurrentHand(hand)
                syncAnimation(user, world, stack, EXTEND)
            }

            TypedActionResult.consume(stack)
        },
        client = {
            super.use(world, user, hand)
        }
    )

    private fun attemptToGrab(
        mgr: GrabbingManager,
        user: ServerPlayerEntity,
        world: ServerWorld,
        config: GravityGunsConfig,
        power: Double
    ): Boolean =
        GrabUtil.attemptToGrabEntity(mgr, user, config.entityReachDistance) ||
        GrabUtil.attemptToGrabBlock(mgr, user, world, config.blockReachDistance, power)


    override fun registerControllers(data: AnimationData) {
        val controller = AnimationController(this, CONTROLLER_ID, 0.02f) { PlayState.CONTINUE }
        controller.setAnimation {
            it.addAnimation("animation.gravity_gun.closed")
        }
        // TODO: remind gecko to merge my PR that fixes this bullshit
        controller.registerSoundListener(object : AnimationController.ISoundListener {
            override fun <A : IAnimatable> playSound(event: SoundKeyframeEvent<A>) {
                // please, just
                // spare my life geckolib
                if (event.controller.animationState == AnimationState.Transitioning) return

                val player = MinecraftClient.getInstance().player
                // look i'm fucking tired alright
                val volume = if (event.sound == "item.gravity_gun.woo") 0.1f else 1f

                player?.playSound(SoundEvent(GravityGuns.id(event.sound)), volume, 1f)
            }

        })
        data.addAnimationController(controller)
    }

    override fun getFactory(): AnimationFactory = factory

    private fun syncAnimation(user: PlayerEntity, world: World, stack: ItemStack, anim: Int) = splitSide(user, world,
        server = {
            val item = this@GravityGunItem

            val id = GeckoLibUtil.guaranteeIDForStack(stack, it.world)
            GeckoLibNetwork.syncAnimation(user, item, id, anim)
            for (otherPlayer in PlayerLookup.tracking(user)) {
                GeckoLibNetwork.syncAnimation(otherPlayer, item, id, anim)
            }
        },
        client = {
            throw IllegalStateException("syncAnimation may *only* be called on the logical server")
        }
    )

    override fun onAnimationSync(id: Int, state: Int) {
        val controller = GeckoLibUtil.getControllerForID(factory, id, CONTROLLER_ID)
        //controller.clearAnimationCache()
        controller.markNeedsReload()
        controller.setAnimation {
            when (state) {
                EXTEND -> it
                    .addAnimation("animation.gravity_gun.extend")
                    .addAnimation("animation.gravity_gun.open")
                RETRACT -> it
                    .addAnimation("animation.gravity_gun.retract")
                    .addAnimation("animation.gravity_gun.closed")
            }
        }
    }
}