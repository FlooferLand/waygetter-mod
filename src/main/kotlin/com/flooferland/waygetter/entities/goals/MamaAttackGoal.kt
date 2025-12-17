package com.flooferland.waygetter.entities.goals

import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.ai.goal.*
import com.flooferland.waygetter.entities.MamaEntity
import com.flooferland.waygetter.registry.ModSounds
import com.flooferland.waygetter.systems.NoiseTracker
import com.flooferland.waygetter.utils.Extensions.isProvokingMama

class MamaAttackGoal(val mama: MamaEntity) : Goal() {
    var attackCooldown: Int = 0  // ticks

    fun getAttackable() = mama.attackLine.getFirst()

    override fun canUse(): Boolean {
        return getAttackable() != null
    }

    override fun tick() {
        if (attackCooldown > 0) { attackCooldown -= 1; return }
        val attackable = getAttackable() ?: return
        val canAttack = !mama.isInvisible && attackable.isProvokingMama() && (NoiseTracker.get(attackable) > NoiseTracker.NOISE_QUIET)
        if (attackCooldown == 0 && canAttack) {
            mama.doHurtTarget(attackable)

            if (attackable.isDeadOrDying) {
                mama.level().playSound(null, mama.blockPosition(), ModSounds.MamaJumpscare.event, SoundSource.HOSTILE, 1f, 1f)
                mama.remove(Entity.RemovalReason.DISCARDED)
            }
            attackCooldown = 5
        }
    }
}