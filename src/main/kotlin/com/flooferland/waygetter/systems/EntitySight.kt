package com.flooferland.waygetter.systems

import net.minecraft.server.level.*
import net.minecraft.world.entity.*

// TODO: Check if the entity is on the player's screen

/**
 * Detects if the entity can be seen by the player.
 * Uses [EntityLine] internally.
 */
class EntitySight(private val owner: Entity, val maxDist: Double) {
    val maxDistSqrt = maxDist * maxDist

    private val line = EntityLine(owner, maxDist)

    fun firstSeen(): ServerPlayer? {
        val level = owner.level() as? ServerLevel ?: return null
        for (player in level.players()) {
            if (player.distanceToSqr(owner) > maxDistSqrt) continue
            if (seenBy(player)) return player
        }
        return null
    }

    fun seenByAnyone() = firstSeen() != null

    /** Directly raycasts to the player and checks if the entity is in view */
    fun seenBy(player: ServerPlayer): Boolean {
        if (!line.hasLineTo(player)) return false

        val entityViewPos = owner.boundingBox.center
        val playerViewPos = player.eyePosition
        val playerLook = player.lookAngle

        val toSelf = entityViewPos.subtract(playerViewPos).normalize()
        val dot = playerLook.dot(toSelf)
        return dot > 0f
    }
}