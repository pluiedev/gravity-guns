package com.leocth.gravityguns.client.render.entity

import com.leocth.gravityguns.entity.ClumpEntity
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.render.entity.EntityRendererFactory
import net.snakefangox.worldshell.client.WorldShellEntityRenderer

@Environment(EnvType.CLIENT)
class ClumpEntityRenderer(ctx: EntityRendererFactory.Context) : WorldShellEntityRenderer<ClumpEntity>(ctx)