package com.flooferland.waygetter.utils

import java.util.concurrent.ThreadLocalRandom
import java.util.random.RandomGenerator
import kotlin.random.asKotlinRandom

object WaygetterRandom {
    private val random = ThreadLocalRandom.from(RandomGenerator.getDefault())!!.asKotlinRandom()

    fun nextDouble(): Double =
        random.nextDouble()
    fun nextFloat(): Float =
        random.nextFloat()
    fun nextInt(min: Int, max: Int): Int =
        random.nextInt(min, max)
}