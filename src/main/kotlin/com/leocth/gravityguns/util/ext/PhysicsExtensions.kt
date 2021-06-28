@file:Suppress("NOTHING_TO_INLINE")
package com.leocth.gravityguns.util.ext

import com.jme3.math.Vector3f
import dev.lazurite.rayon.core.impl.bullet.math.VectorHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3f

// Kotlin-friendly version of VectorHelper. All inlined, of course.

inline fun Vec3f.toBullet(): Vector3f
    = Vector3f(x, y, z)
inline fun Vec3d.toBullet(): Vector3f
    = Vector3f(x.toFloat(), y.toFloat(), z.toFloat())

inline fun Vector3f.toVec3f(): Vec3f
    = Vec3f(x, y, z)
inline fun Vector3f.toVec3d(): Vec3d
    = Vec3d(x.toDouble(), y.toDouble(), z.toDouble())

