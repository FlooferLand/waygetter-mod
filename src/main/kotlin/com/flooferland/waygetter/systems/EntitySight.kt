package com.flooferland.waygetter.systems

import net.minecraft.server.level.*
import net.minecraft.world.entity.*

// TODO: Check if the entity is on the player's screen

/**
 * Detects if the entity can be seen by the player.
 * Uses [EntityLine] internally.
 */
class EntitySight(private val owner: Entity, val maxDist: Double) {
    private val line = EntityLine(owner, maxDist)

    fun firstSeen(): ServerPlayer? {
        return line.getFirst()
    }

    fun seenByAnyone(): Boolean {
        return line.hasLineToAny()
    }

    fun seenBy(player: ServerPlayer): Boolean {
        return line.hasLineTo(player)
    }
}