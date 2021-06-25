package com.leocth.gravityguns.util.ext

import software.bernie.geckolib3.core.IAnimatable
import software.bernie.geckolib3.core.builder.AnimationBuilder
import software.bernie.geckolib3.core.controller.AnimationController

inline fun <T : IAnimatable> AnimationController<T>.setAnimation(builder: (AnimationBuilder) -> Unit) {
    setAnimation(AnimationBuilder().apply(builder))
}