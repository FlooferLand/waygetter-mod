package com.flooferland.waygetter.entities

import net.minecraft.core.*
import net.minecraft.server.level.*
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.GameEventTags
import net.minecraft.world.entity.*
import net.minecraft.world.entity.monster.*
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.*
import net.minecraft.world.level.gameevent.*
import net.minecraft.world.level.gameevent.vibrations.*
import net.minecraft.world.level.pathfinder.PathType
import net.minecraft.world.phys.Vec3
import com.flooferland.waygetter.entities.goals.MamaAttackGoal
import com.flooferland.waygetter.entities.goals.MamaChaseGoal
import com.flooferland.waygetter.items.MamaItem.Companion.REGISTER_CONTROLLERS
import com.flooferland.waygetter.registry.ModEntities
import com.flooferland.waygetter.registry.ModSounds
import com.flooferland.waygetter.systems.EntityLine
import com.flooferland.waygetter.systems.EntitySight
import com.flooferland.waygetter.systems.NoiseTracker
import java.util.function.BiConsumer
import software.bernie.geckolib.animatable.GeoEntity
import software.bernie.geckolib.animation.AnimatableManager
import software.bernie.geckolib.util.GeckoLibUtil
import kotlin.math.sqrt

class MamaEntity(level: Level) : Monster(ModEntities.Mama.type, level), GeoEntity {
    val maxDist = 64.0
    val maxDistSqrt = maxDist * maxDist
    val attackLine = EntityLine(this, 2.5)
    val line = EntityLine(this, maxDist)
    val sight = EntitySight(this, maxDist)
    val cache = GeckoLibUtil.createInstanceCache(this)!!

    override fun getAnimatableInstanceCache() = cache
    override fun registerControllers(controllers: AnimatableManager.ControllerRegistrar) {
        REGISTER_CONTROLLERS(this, controllers)
    }

    //region Vibrations
    private val vibrationListener = MamaVibrationListener()
    private val eventListener = DynamicGameEventListener(vibrationListener)
    override fun dampensVibrations() = true
    // endregion

    override fun isPushable() = false
    override fun removeWhenFarAway(distanceToClosestPlayer: Double) = false

    override fun getAmbientSound() = ModSounds.MamaTaunt.event
    override fun getSoundVolume() = 0.5f

    init {
        setPathfindingMalus(PathType.UNPASSABLE_RAIL, 0.0f)
        setPathfindingMalus(PathType.DAMAGE_OTHER, 8.0f)
        setPathfindingMalus(PathType.POWDER_SNOW, 8.0f)
        setPathfindingMalus(PathType.LAVA, 8.0f)
        setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0f)
        setPathfindingMalus(PathType.DANGER_FIRE, 0.0f)
    }

    override fun registerGoals() {
        goalSelector.addGoal(0, MamaAttackGoal(this))
        goalSelector.addGoal(1, MamaChaseGoal(this))
    }

    override fun tick() {
        super.tick()
        val level = level() as? ServerLevel ?: return

    }

    override fun updateDynamicGameEventListener(consumer: BiConsumer<DynamicGameEventListener<*>?, ServerLevel?>) {
        val level = level() as? ServerLevel ?: return
        consumer.accept(eventListener, level)
    }

    inner class MamaVibrationListener : GameEventListener {
        val source = EntityPositionSource(this@MamaEntity, this@MamaEntity.eyeHeight)
        override fun getListenerSource() = source
        override fun getListenerRadius() = maxDist.toInt()

        override fun handleGameEvent(level: ServerLevel, gameEvent: Holder<GameEvent>, context: GameEvent.Context, pos: Vec3): Boolean {
            if (!gameEvent.`is`(GameEventTags.WARDEN_CAN_LISTEN)) return false
            if (this@MamaEntity.isNoAi || this@MamaEntity.isDeadOrDying) return false
            val player = context.sourceEntity as? ServerPlayer ?: return false
            NoiseTracker.updateVibration(player, gameEvent)
            return true
        }
    }
}