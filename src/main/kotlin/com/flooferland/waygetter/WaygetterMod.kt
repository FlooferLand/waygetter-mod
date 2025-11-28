package com.flooferland.waygetter

import com.flooferland.waygetter.registry.ModComponents
import com.flooferland.waygetter.registry.ModEntities
import com.flooferland.waygetter.registry.ModItemGroups
import com.flooferland.waygetter.registry.ModItems
import com.flooferland.waygetter.registry.ModPackets
import com.flooferland.waygetter.registry.ModSounds
import com.flooferland.waygetter.systems.tattletail.TattleManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.full.createInstance

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

        @Suppress("UnusedExpression")
        run {
            ModComponents
            ModItems.entries
            ModSounds.entries
            ModEntities

            TattleManager
            ModItemGroups.entries  // should be at the end
            ModPackets.registerS2C()
        }
    }
}