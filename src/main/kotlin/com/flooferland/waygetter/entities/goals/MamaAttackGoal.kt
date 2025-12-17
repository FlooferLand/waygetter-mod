package com.flooferland.waygetter.entities.goals

import net.minecraft.sounds.SoundSource
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.ai.goal.*
import com.flooferland.waygetter.entities.MamaEntity
import com.flooferland.waygetter.registry.ModSounds
import com.flooferland.waygetter.systems.NoiseTracker
import com.flooferland.waygetter.utils.Extensions.isProvokingMama
import com.flooferland.waygetter.utils.Extensions.secsToTicks

class MamaAttackGoal(val mama: MamaEntity) : Goal() {
    var ticksStressed: Int = 0

    fun getAttackable() = mama.attackLine.getFirst()

    override fun canUse(): Boolean {
        return getAttackable() != null
    }

    override fun tick() {
        val attackable = getAttackable() ?: return
        if (ticksStressed < 1.secsToTicks()) {
            ticksStressed += 1
            return
        } else if (ticksStressed > 0) {
            ticksStressed -= 1
        }

        val canAttack = !mama.isInvisible && attackable.isProvokingMama() && (NoiseTracker.get(attackable) > NoiseTracker.NOISE_MEDIUM)
        if (canAttack) {
            mama.doHurtTarget(attackable)

            attackable.addEffect(MobEffectInstance(MobEffects.DARKNESS, 5.secsToTicks(), 5, true, false))
            attackable.addEffect(MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 3.secsToTicks(), 2, true, false))
            if (attackable.isDeadOrDying) {
                attackable.addEffect(MobEffectInstance(MobEffects.DARKNESS, 10.secsToTicks(), 2, true, false))
                attackable.addEffect(MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10.secsToTicks(), 5, true, false))
                mama.level().playSound(null, mama.blockPosition(), ModSounds.MamaJumpscare.event, SoundSource.HOSTILE, 1f, 1f)
                mama.remove(Entity.RemovalReason.DISCARDED)
            }
        }
    }
}