@file:Environment(EnvType.CLIENT)
package com.leocth.gravityguns.util.ext

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.util.math.MatrixStack

inline fun MatrixStack.frame(frame: (MatrixStack) -> Unit) {
    push()
    frame(this)
    pop()
}

