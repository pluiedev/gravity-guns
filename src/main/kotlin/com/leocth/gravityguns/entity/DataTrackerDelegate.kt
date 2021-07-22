package com.leocth.gravityguns.entity

import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import kotlin.reflect.KProperty

data class DataTrackerDelegate<T>(val tracker: DataTracker, val key: TrackedData<T>) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T =
        tracker[key]

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        tracker[key] = value
    }
}

inline fun <reified T> DataTracker.byKey(key: TrackedData<T>): DataTrackerDelegate<T> =
    DataTrackerDelegate(this, key)