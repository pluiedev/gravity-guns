package com.leocth.gravityguns.client.render.entity

import com.jme3.math.Quaternion
import com.leocth.gravityguns.entity.BlockAsAnEntity
import com.leocth.gravityguns.util.ext.component1
import com.leocth.gravityguns.util.ext.component2
import com.leocth.gravityguns.util.ext.component3
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import com.leocth.gravityguns.util.ext.frame
import net.minecraft.block.BlockRenderType
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.render.block.BlockRenderManager
import net.minecraft.util.math.Quaternion as QuaternionMC

@Environment(EnvType.CLIENT)
class BlockAsAnEntityRenderer(ctx: EntityRendererFactory.Context) : EntityRenderer<BlockAsAnEntity>(ctx) {

    private val blockRenderManager: BlockRenderManager = MinecraftClient.getInstance().blockRenderManager
    private var tempQuat: Quaternion = Quaternion()
    private var tempQuatMc: QuaternionMC = QuaternionMC(0f, 0f, 0f, 0f)

    init { shadowRadius = 0.5f }

    override fun render(
        entity: BlockAsAnEntity,
        yaw: Float,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ) {
        val mesh = entity.mesh ?: return
        val states = entity.states
        val world = entity.world
        val blockPos = entity.blockPos

        matrices.frame { stack ->
            val rigidBody = entity.rigidBody
            rigidBody.getPhysicsRotation(tempQuat)
            tempQuatMc.set(tempQuat.x, tempQuat.y, tempQuat.z, tempQuat.w)
            stack.multiply(tempQuatMc)

            val (oX, oY, oZ) = states.offset
            stack.translate(oX - 0.5, oY - 0.5, oZ - 0.5)

            mesh.render(matrices.peek().model)

            /*
            states.forEach { _, _, _, pos, state ->
                if (state.renderType == BlockRenderType.MODEL) {
                    val l = WorldRenderer.getLightmapCoordinates(world, state, pos)
                    matrices.frame {
                        it.translate(pos.x-0.5, pos.y-0.5, pos.z-0.5)
                        blockRenderManager.modelRenderer.render(
                            world,
                            blockRenderManager.getModel(state),
                            state,
                            blockPos,
                            it,
                            vertexConsumers.getBuffer(RenderLayers.getMovingBlockLayer(state)),
                            false,
                            world.random,
                            114514, // TODO
                            OverlayTexture.DEFAULT_UV
                        )

                    }
                    super.render(entity, yaw, tickDelta, matrices, vertexConsumers, l)
                }
            }

             */
        }


    }

    @Suppress("DEPRECATION")
    override fun getTexture(entity: BlockAsAnEntity): Identifier = SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE
}

