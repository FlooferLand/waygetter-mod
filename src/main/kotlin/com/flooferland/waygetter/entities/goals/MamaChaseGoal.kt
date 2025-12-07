package com.flooferland.waygetter.entities.goals

import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.ai.goal.Goal
import com.flooferland.waygetter.entities.MamaEntity
import com.flooferland.waygetter.items.TattletailItem

class MamaChaseGoal(val mama: MamaEntity) : Goal() {
    var victim: ServerPlayer? = null

    override fun canUse(): Boolean {
        if (mama.sight.seenByAnyone()) return false
        val level = mama.level() as? ServerLevel ?: return false
        return level.players().any { player ->
            player.distanceToSqr(mama) < mama.maxDist * mama.maxDist
            && player.inventory.hasAnyMatching { it.item is TattletailItem }
        }
    }

    override fun start() {
        if (victim == null) {
            val level = mama.level() as? ServerLevel ?: return
            victim = level.players().first { player ->
                player.distanceToSqr(mama) < mama.maxDist * mama.maxDist
                && player.inventory.hasAnyMatching { it.item is TattletailItem }
            }
        }
        victim?.let { mama.navigation.moveTo(it, 0.5) }
    }

    override fun stop() {
        mama.navigation.stop()
        victim = null
    }

    override fun tick() {

    }
}