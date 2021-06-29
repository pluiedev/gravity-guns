package com.leocth.gravityguns.client.render.entity

import com.leocth.gravityguns.entity.BlockAsAnEntity
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import com.leocth.gravityguns.util.ext.component1
import com.leocth.gravityguns.util.ext.component2
import com.leocth.gravityguns.util.ext.component3
import com.leocth.gravityguns.util.ext.frame
import net.minecraft.block.BlockRenderType
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayers
import net.minecraft.util.math.BlockPos

@Environment(EnvType.CLIENT)
class BlockAsAnEntityRenderer(ctx: EntityRendererFactory.Context) : EntityRenderer<BlockAsAnEntity>(ctx) {
    init { shadowRadius = 0.5f }

    override fun render(
        entity: BlockAsAnEntity,
        yaw: Float,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ) {
        val states = entity.states

        val world = entity.world
        val blockPos = BlockPos(entity.x, entity.boundingBox.maxY, entity.z)
        val blockRenderManager = MinecraftClient.getInstance().blockRenderManager

        // TODO: this is horrible for performance. use a baked model.
        states.forEach { _, _, _, pos, state ->
            if (state.renderType == BlockRenderType.MODEL) {
                matrices.frame {
                    it.translate(-0.5, -0.5, -0.5)
                    it.translate(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
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
                super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun getTexture(entity: BlockAsAnEntity): Identifier = SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE
}

