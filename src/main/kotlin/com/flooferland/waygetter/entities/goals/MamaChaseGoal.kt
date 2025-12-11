package com.flooferland.waygetter.entities.goals

import net.minecraft.core.BlockPos
import net.minecraft.server.level.*
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.ai.goal.*
import net.minecraft.world.phys.Vec3
import com.flooferland.waygetter.entities.MamaEntity
import com.flooferland.waygetter.registry.ModSounds
import com.flooferland.waygetter.utils.Extensions.isProvokingMama
import com.flooferland.waygetter.utils.Extensions.secsToTicks
import com.flooferland.waygetter.utils.WaygetterUtils
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class MamaChaseGoal(val mama: MamaEntity) : Goal() {
    var lastNavTick = initialCooldown
    var victim: ServerPlayer? = null
    var victimLastPos: Vec3? = null

    companion object {
        const val TP_DIST_MIN: Double = 5.0
        const val TP_DIST_MAX: Double = 15.0
        const val TP_MAX_ATTEMPTS: Int = 30
        const val TP_Y_TOLERANCE: Int = 5
        val moveChance = 8.secsToTicks()
        val initialCooldown = 10.secsToTicks()
    }

    override fun canUse(): Boolean {
        if (mama.sight.seenByAnyone()) return false
        val level = mama.level() as? ServerLevel ?: return false
        return level.players().any { player ->
            player.distanceToSqr(mama) < mama.maxDist * mama.maxDist
            && player.isProvokingMama()
        }
    }

    override fun start() {
        if (victim == null) {
            val level = mama.level() as? ServerLevel ?: return
            victim = level.players().first { player ->
                player.distanceToSqr(mama) < mama.maxDist * mama.maxDist
                && player.isProvokingMama()
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
        if (mama.tickCount - lastNavTick >= moveChance) {
            tickUnseen(level)
            lastNavTick = mama.tickCount
        }
    }

    /** Called while no player is looking */
    private fun tickUnseen(level: ServerLevel) {
        val victim = victim ?: return
        val victimLastPos = victimLastPos ?: victim.position()
        val makingProgress = mama.distanceToSqr(victimLastPos) < mama.distanceToSqr(victim.position())

        if (WaygetterUtils.random.nextIntBetweenInclusive(0, 6) == 3) {
            tryMove(level, victim)
        }
        this.victimLastPos = victim.position()
    }

    private fun tryMove(level: ServerLevel, victim: ServerPlayer) {
        val playerVel = victim.deltaMovement ?: return
        val moveDir = if (playerVel.horizontalDistanceSqr() > 0.001) playerVel.normalize() else victim.lookAngle
        val baseAngle = atan2(moveDir.z, moveDir.x)

        for (attempt in 0..TP_MAX_ATTEMPTS) {
            val angle = baseAngle + WaygetterUtils.random.nextDouble() * 2.0 * PI
            val dist = WaygetterUtils.random.nextDouble() * (TP_DIST_MAX - TP_DIST_MIN) + TP_DIST_MIN
            val target = victim.position().add(cos(angle) * dist, 0.0, sin(angle) * dist)

            findMovePos(level, victim, target)?.let { pos ->
                mama.moveTo(pos)
                level.playSound(null, mama.blockPosition(), ModSounds.MamaTaunt.event, SoundSource.NEUTRAL)
                return
            }
        }
    }

    private fun findMovePos(level: ServerLevel, victim: ServerPlayer, target: Vec3): Vec3? {
        val blockPos = BlockPos.containing(target)

        for (offset in -5..5) {
            val pos = blockPos.offset(0, offset, 0)
            if (isPosValid(level, victim, pos)) {
                return pos.bottomCenter
            }
        }
        return null
    }

    private fun isPosValid(level: ServerLevel, victim: ServerPlayer, pos: BlockPos): Boolean {
        // Solid ground
        if (!level.getBlockState(pos.below()).isSolidRender(level, pos.below())) return false

        // Same space as the player
        val underRoof = !level.canSeeSky(pos)
        if (underRoof != !level.canSeeSky(victim.blockPosition())) return false
        if (underRoof) {
            // Not under a hanging roof outside
            for (y in 1..10) {
                val checkPos = pos.offset(0, y, 0)
                if (!level.getBlockState(checkPos).isAir) {
                    if (level.getBlockState(checkPos.north()).isAir) return false
                    if (level.getBlockState(checkPos.east()).isAir) return false
                    if (level.getBlockState(checkPos.south()).isAir) return false
                    if (level.getBlockState(checkPos.west()).isAir) return false
                }
            }
        }

        // Enough vertical space
        if (!level.getBlockState(pos).isAir) return false
        if (!level.getBlockState(pos.above()).isAir) return false

        // Same-ish Y as the player
        if (abs(pos.y - victim.blockY) > TP_Y_TOLERANCE) return false

        // Safe terrain
        val feetState = level.getBlockState(pos)
        if (!feetState.fluidState.isEmpty) return false

        // Floor
        if (!isFloorValid(level, victim, pos)) return false

        return true
    }

    private fun isFloorValid(level: ServerLevel, victim: ServerPlayer, pos: BlockPos): Boolean {
        val minY = min(pos.y, victim.blockY)
        val maxY = max(pos.y, victim.blockY)
        if (maxY - minY < 2) return true

        for (y in minY + 1 until maxY) {
            val checkPos = BlockPos(pos.x, y, pos.z)
            if (level.getBlockState(checkPos).isSolidRender(level, checkPos)) {
                return false
            }
        }

        return true
    }
}