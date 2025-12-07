package com.flooferland.waygetter.systems

import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.HitResult

/**
 * Detects if the entity can reach a line to the player.
 * Useful to detect if the player is in reach to be attacked by the entity.
 */
class EntityLine(private val owner: Entity, val maxDist: Double) {
    /** Returns all players it has a line to */
    fun getAll(): List<ServerPlayer> {
        val level = owner.level() as? ServerLevel ?: return listOf()
        val picked = mutableListOf<ServerPlayer>()
        for (player in level.players()) {
            if (player.distanceToSqr(owner) > maxDist * maxDist) continue
            if (hasLineTo(player)) picked.add(player)
        }
        return picked
    }

    /** Returns the first player it has a line to */
    fun getFirst(): ServerPlayer? {
        val level = owner.level() as? ServerLevel ?: return null
        for (player in level.players()) {
            if (player.distanceToSqr(owner) > maxDist * maxDist) continue
            if (hasLineTo(player)) return player
        }
        return null
    }

    /** Returns true if it has a line to any player */
    fun hasLineToAny() = getFirst() != null

    /** Directly raycasts to the player */
    fun hasLineTo(player: ServerPlayer): Boolean {
        val level = player.level() as? ServerLevel ?: return false
        val start = player.getEyePosition()
        val end = owner.position().add(0.0, 0.5, 0.0)
        if (start.distanceTo(end) > maxDist) return false

        val hit = level.clip(ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player))
        if (hit.type == HitResult.Type.MISS) {
            return true
        } else if (hit.type == HitResult.Type.BLOCK) {
            val state = level.getBlockState(hit.blockPos)
            return state.isAir || !state.canOcclude()
        }
        return false
    }
}