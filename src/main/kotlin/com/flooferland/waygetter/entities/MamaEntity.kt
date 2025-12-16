package com.flooferland.waygetter.entities

import net.minecraft.core.*
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.*
import net.minecraft.tags.GameEventTags
import net.minecraft.world.entity.monster.*
import net.minecraft.world.level.*
import net.minecraft.world.level.gameevent.*
import net.minecraft.world.level.pathfinder.PathType
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import com.flooferland.waygetter.entities.goals.MamaAttackGoal
import com.flooferland.waygetter.entities.goals.MamaChaseGoal
import com.flooferland.waygetter.items.FlashlightItem
import com.flooferland.waygetter.items.MamaItem.Companion.REGISTER_CONTROLLERS
import com.flooferland.waygetter.registry.ModEntities
import com.flooferland.waygetter.registry.ModSounds
import com.flooferland.waygetter.registry.ModSynchedData
import com.flooferland.waygetter.systems.EntityLine
import com.flooferland.waygetter.systems.EntitySight
import com.flooferland.waygetter.systems.NoiseTracker
import com.flooferland.waygetter.utils.Extensions.isProvokingMama
import com.flooferland.waygetter.utils.playerMadeSound
import java.util.function.BiConsumer
import software.bernie.geckolib.animatable.GeoEntity
import software.bernie.geckolib.animation.AnimatableManager
import software.bernie.geckolib.util.GeckoLibUtil

class MamaEntity(level: Level) : Monster(ModEntities.Mama.type, level), GeoEntity {
    companion object {
        const val MAX_DIST = 64.0
        const val MAX_DIST_SQRT = MAX_DIST * MAX_DIST
    }

    val attackLine = EntityLine(this, 2.5)
    val line = EntityLine(this, MAX_DIST)
    val sight = EntitySight(this, MAX_DIST)
    val cache = GeckoLibUtil.createInstanceCache(this)!!

    var victim: ServerPlayer? = null
    var invisOnSpawn = false
        set(value) {
            field = value
            isInvisible = value
        }

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
    override fun playAmbientSound() {
        val victim = victim ?: return
        if (victim.isProvokingMama()) {
            super.playAmbientSound()
        }
    }

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

        // Setting the victim
        if (victim?.isProvokingMama() ?: false) {
            victim = null
        }
        val victim = victim ?:
            level.players().firstOrNull() { player ->
                player.distanceToSqr(this) < MAX_DIST_SQRT
                    && player.isProvokingMama()
            }
        this.victim = victim

        // Spawning in
        if (invisOnSpawn) isInvisible = true
        if (invisOnSpawn && tickCount > 20 && (!line.hasLineToAny() || this.victim != null)) {
            invisOnSpawn = false
            isInvisible = false
        }

        // Killing the flashlight
        victim?.let { victim ->
            if (!victim.isProvokingMama()) return@let
            if (!victim.isHolding { it.item is FlashlightItem }) return@let

            val flashlightRange = 16.0
            val victimLook = victim.lookAngle
            val victimFrom = victim.eyePosition
            val beamEnd = victimFrom.add(victimLook.scale(flashlightRange))
            if (boundingBox.clip(victimFrom, beamEnd).isEmpty) return@let

            val clip = level.clip(ClipContext(victimFrom, boundingBox.center, ClipContext.Block.VISUAL, ClipContext.Fluid.ANY, victim))
            if (clip.type != HitResult.Type.MISS) return@let

            if (victim.entityData.get(ModSynchedData.flashlightBattery) > 0f) {
                victim.entityData.set(ModSynchedData.flashlightBattery, 0f,  true)
                playerMadeSound(level, victim, ModSounds.FlashlightDie)
            }
        }
    }

    override fun readAdditionalSaveData(compound: CompoundTag) {
        super.readAdditionalSaveData(compound)
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        super.addAdditionalSaveData(compound)
    }

    override fun updateDynamicGameEventListener(consumer: BiConsumer<DynamicGameEventListener<*>?, ServerLevel?>) {
        val level = level() as? ServerLevel ?: return
        consumer.accept(eventListener, level)
    }

    inner class MamaVibrationListener : GameEventListener {
        val source = EntityPositionSource(this@MamaEntity, this@MamaEntity.eyeHeight)
        override fun getListenerSource() = source
        override fun getListenerRadius() = MAX_DIST.toInt()

        override fun handleGameEvent(level: ServerLevel, gameEvent: Holder<GameEvent>, context: GameEvent.Context, pos: Vec3): Boolean {
            if (!gameEvent.`is`(GameEventTags.WARDEN_CAN_LISTEN)) return false
            if (this@MamaEntity.isNoAi || this@MamaEntity.isDeadOrDying) return false
            val player = context.sourceEntity as? ServerPlayer ?: return false
            NoiseTracker.updateVibration(player, gameEvent)
            return true
        }
    }
}