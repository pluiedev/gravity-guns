@file:Suppress("NOTHING_TO_INLINE")
package com.leocth.gravityguns.util.ext

import com.jme3.math.Vector3f
import dev.lazurite.rayon.core.impl.bullet.math.VectorHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3f
import net.minecraft.util.math.Vec3i

// Kotlin-friendly version of VectorHelper. All inlined, of course.

inline fun Vec3f.toBullet(): Vector3f
    = Vector3f(x, y, z)
inline fun Vec3d.toBullet(): Vector3f
    = Vector3f(x.toFloat(), y.toFloat(), z.toFloat())

inline fun Vector3f.toVec3f(): Vec3f
    = Vec3f(x, y, z)
inline fun Vector3f.toVec3d(): Vec3d
    = Vec3d(x.toDouble(), y.toDouble(), z.toDouble())

operator fun Vec3f.component1() = x
operator fun Vec3f.component2() = y
operator fun Vec3f.component3() = z

operator fun Vec3d.component1() = x
operator fun Vec3d.component2() = y
operator fun Vec3d.component3() = z

operator fun Vector3f.component1() = x
operator fun Vector3f.component2() = y
operator fun Vector3f.component3() = z

operator fun Vec3i.component1(): Int = x
operator fun Vec3i.component2(): Int = y
operator fun Vec3i.component3(): Int = z