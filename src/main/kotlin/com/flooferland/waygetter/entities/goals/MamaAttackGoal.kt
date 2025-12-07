package com.flooferland.waygetter.entities.goals

import net.minecraft.world.entity.ai.goal.*
import com.flooferland.waygetter.entities.MamaEntity

class MamaAttackGoal(val mama: MamaEntity) : Goal() {
    var attackCooldown: Int = 0  // ticks

    fun getAttackable() = mama.attackLine.getFirst()

    override fun canUse(): Boolean {
        return getAttackable() != null
    }

    override fun tick() {
        if (attackCooldown > 0) { attackCooldown -= 1; return }
        val attackable = getAttackable() ?: return
        if (attackCooldown == 0) {
            attackable.hurt(attackable.damageSources().mobAttack(mama), 1.0f)
            attackCooldown = 10
        }
    }
}