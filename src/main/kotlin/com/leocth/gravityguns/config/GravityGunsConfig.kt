package com.leocth.gravityguns.config

import kotlinx.serialization.Serializable

@Serializable
data class GravityGunsConfig(
    /**
     * The maximum power level the gravity gun can use. This affects how many blocks one can grab.
     */
    var maximumPowerLevel: Int = 5,

    /**
     * The multiplier for the launch initial velocity; the higher this is, the faster it goes.
     */
    var launchInitialVelocityMultiplier: Double = 20.0,

    /**
     * The reach distance when finding entities to grab.
     *
     * **CAUTION**: this value is *ONLY* read in servers - clients cannot override this to achieve any effect!
     */
    var entityReachDistance: Double = 7.0,

    /**
     * The reach distance when finding blocks to grab.
     *
     * **CAUTION**: this value is *ONLY* read in servers - clients cannot override this to achieve any effect!
     */
    var blockReachDistance: Double = 8.0,
)
