package com.flooferland.waygetter.entities.goals

import net.minecraft.server.level.*
import net.minecraft.sounds.*
import net.minecraft.world.entity.ai.goal.*
import net.minecraft.world.phys.Vec3
import com.flooferland.waygetter.entities.MamaEntity
import com.flooferland.waygetter.items.TattletailItem
import com.flooferland.waygetter.registry.ModSounds
import com.flooferland.waygetter.utils.Extensions.ticks
import com.flooferland.waygetter.utils.WaygetterUtils

class MamaChaseGoal(val mama: MamaEntity) : Goal() {
    var victim: ServerPlayer? = null
    var lastNavTick = initialCooldown
    var victimLastPos: Vec3? = null

    companion object {
        val initialCooldown = 20.ticks()
    }

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
        victim?.let { mama.navigation.moveTo(it, 1.0) }
        lastNavTick = initialCooldown
    }

    override fun stop() {
        mama.navigation.stop()
        victim = null
    }

    override fun tick() {
        val level = mama.level() as? ServerLevel ?: return
        val victim = victim ?: return
        val victimLastPos = victimLastPos ?: victim.position()
        if (mama.tickCount - lastNavTick >= 20) {
            val makingProgress = mama.distanceToSqr(victimLastPos) < mama.distanceToSqr(victim.position())

            if (WaygetterUtils.random.nextIntBetweenInclusive(0, 10) == 3) {
                // Attempt close teleport
                val startPos = mama.position()
                mama.isInvisible = true
                for (i in 0..50) {
                    val basePos = if (!makingProgress || mama.position().distanceToSqr(victim.position()) > startPos.distanceToSqr(victim.position())) {
                        victim.position()
                    } else {
                        startPos
                    }
                    mama.randomTeleport(basePos.x, basePos.y, basePos.z, false)
                    if (!mama.sight.seenByAnyone() && mama.position().distanceToSqr(victim.position()) > mama.attackLine.maxDistSqrt) {
                        level.playSound(null, mama.blockPosition(), ModSounds.MamaTaunt.event, SoundSource.NEUTRAL)
                        break
                    }
                }
                mama.isInvisible = false
            }
        }
        this.victimLastPos = victim.position()
        lastNavTick = mama.tickCount
    }
}