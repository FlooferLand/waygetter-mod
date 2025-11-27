package com.flooferland.waygetter

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object WaygetterMod {
    const val MOD_ID = "waygetter"
    val log: Logger = LoggerFactory.getLogger(MOD_ID)
    val initMessage = arrayOf(
        "Me Tattletail, me love you!",
        "Orange? Orange who?",
        "Ahh, big sound!",
        "Light the candles!"
    )

    public fun initialize() {
        initMessage.randomOrNull()?.let { log.info(it) }
    }
}