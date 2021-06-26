package com.leocth.gravityguns.client.render.item

import com.leocth.gravityguns.GravityGuns
import com.leocth.gravityguns.item.GravityGunItem
import com.leocth.gravityguns.util.ext.frame
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtHelper
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Quaternion
import software.bernie.geckolib3.model.AnimatedGeoModel
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer

@Environment(EnvType.CLIENT)
class GravityGunRenderer: GeoItemRenderer<GravityGunItem>(GravityGunModel) {
    private var ticks = 0

    override fun render(
        stack: ItemStack,
        mode: ModelTransformation.Mode,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        super.render(stack, mode, matrices, vertexConsumers, light, overlay)
        ticks++

        // draw floating block
        if (mode != ModelTransformation.Mode.GUI) {
            val client = MinecraftClient.getInstance() ?: return

            val tag = stack.tag ?: return
            val state = NbtHelper.toBlockState(tag.getCompound("block"))

            matrices.frame {
                val y = 0.1 * MathHelper.sin(ticks / 100f) - 0.05

                it.translate(0.0, y, -10.0)
                it.scale(2.0f, 2.0f, 2.0f)
                it.multiply(Quaternion(10f, 10f, 10f, true))
                client.blockRenderManager.renderBlockAsEntity(state, it, vertexConsumers, light, overlay)
            }
        }
    }
}

@Environment(EnvType.CLIENT)
object GravityGunModel: AnimatedGeoModel<GravityGunItem>() {
    override fun getModelLocation(item: GravityGunItem) = GravityGuns.id("geo/item/gravity_gun.geo.json")

    override fun getTextureLocation(item: GravityGunItem) = GravityGuns.id("textures/item/gravity_gun.png")

    override fun getAnimationFileLocation(item: GravityGunItem): Identifier = GravityGuns.id("animations/item/gravity_gun.animation.json")
}