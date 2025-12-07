package com.flooferland.waygetter.entities

import net.minecraft.core.*
import net.minecraft.server.level.*
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.*
import net.minecraft.world.entity.monster.*
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.*
import net.minecraft.world.level.gameevent.*
import net.minecraft.world.level.gameevent.vibrations.*
import com.flooferland.waygetter.entities.goals.MamaAttackGoal
import com.flooferland.waygetter.entities.goals.MamaChaseGoal
import com.flooferland.waygetter.items.MamaItem.Companion.REGISTER_CONTROLLERS
import com.flooferland.waygetter.registry.ModEntities
import com.flooferland.waygetter.registry.ModSounds
import com.flooferland.waygetter.systems.EntityLine
import com.flooferland.waygetter.systems.EntitySight
import software.bernie.geckolib.animatable.GeoEntity
import software.bernie.geckolib.animation.AnimatableManager
import software.bernie.geckolib.util.GeckoLibUtil

class MamaEntity(level: Level) : Monster(ModEntities.Mama.type, level), GeoEntity, VibrationSystem {
    val maxDist = 64.0
    val attackLine = EntityLine(this, 2.0)
    val line = EntityLine(this, maxDist)
    val sight = EntitySight(this, maxDist)
    val cache = GeckoLibUtil.createInstanceCache(this)!!

    override fun getAnimatableInstanceCache() = cache
    override fun registerControllers(controllers: AnimatableManager.ControllerRegistrar) {
        REGISTER_CONTROLLERS(this, controllers)
    }

    override fun getVibrationData() = VibrationSystem.Data()
    override fun getVibrationUser() = VibrationUser()

    override fun isPushable() = false
    override fun removeWhenFarAway(distanceToClosestPlayer: Double) = false

    override fun getAmbientSound() = ModSounds.MamaTaunt.event
    override fun getSoundVolume() = 0.5f

    override fun registerGoals() {
        goalSelector.addGoal(0, MamaAttackGoal(this))
        goalSelector.addGoal(1, MamaChaseGoal(this))
    }

    inner open class VibrationUser : VibrationSystem.User {
        private val positionSource: PositionSource = EntityPositionSource(this@MamaEntity, this@MamaEntity.getEyeHeight())
        override fun getListenerRadius() = 24
        override fun getPositionSource() = positionSource

        override fun canReceiveVibration(level: ServerLevel, pos: BlockPos, gameEvent: Holder<GameEvent>, context: GameEvent.Context): Boolean {
            return true
        }

        override fun canTriggerAvoidVibration(): Boolean {
            return true
        }

        override fun onReceiveVibration(level: ServerLevel, pos: BlockPos, gameEvent: Holder<GameEvent>, entity: Entity?, player: Entity?, distance: Float) {
            println("VIBRATED")
            val player = player as? Player ?: return

            level.playSound(null, pos, ModSounds.MamaTaunt.event, SoundSource.HOSTILE, 1.0f, 1.0f)
        }
    }
}