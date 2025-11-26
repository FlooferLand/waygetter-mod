package com.flooferland.tattletail.loaders

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object TattleMod {
    const val MOD_ID = "showbiz"
    val log: Logger = LoggerFactory.getLogger(MOD_ID)
    val initMessage = arrayOf(
        "Me Tattletail, me love you!",
        "Orange? Orange who?",
        "Ahh, big sound!",
        "Light the candles!"
    )

    public fun initialize() {
        log.info(initMessage.random())
    }
}