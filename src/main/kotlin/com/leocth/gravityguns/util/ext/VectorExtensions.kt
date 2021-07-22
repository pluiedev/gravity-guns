@file:Suppress("NOTHING_TO_INLINE")
package com.leocth.gravityguns.util.ext

import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3f
import net.minecraft.util.math.Vec3i

// Kotlin-friendly version of VectorHelper. All inlined, of course.

inline operator fun Vec3f.component1() = x
inline operator fun Vec3f.component2() = y
inline operator fun Vec3f.component3() = z

inline operator fun Vec3d.component1() = x
inline operator fun Vec3d.component2() = y
inline operator fun Vec3d.component3() = z

inline operator fun Vec3i.component1(): Int = x
inline operator fun Vec3i.component2(): Int = y
inline operator fun Vec3i.component3(): Int = z