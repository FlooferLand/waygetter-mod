package com.flooferland.waygetter.systems.tattletail

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.*
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.*
import net.minecraft.world.level.*
import net.minecraft.world.level.entity.EntityTypeTest
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.HitResult
import com.flooferland.waygetter.components.TattleStateDataComponent
import com.flooferland.waygetter.entities.MamaEntity
import com.flooferland.waygetter.entities.TattletailEntity
import com.flooferland.waygetter.items.FlashlightItem
import com.flooferland.waygetter.items.TattletailItem
import com.flooferland.waygetter.packets.TattleStatePacket
import com.flooferland.waygetter.registry.ModComponents
import com.flooferland.waygetter.registry.ModEntities
import com.flooferland.waygetter.registry.ModSounds
import com.flooferland.waygetter.registry.ModSynchedData
import com.flooferland.waygetter.systems.NoiseTracker
import com.flooferland.waygetter.utils.Extensions.lookAt
import com.flooferland.waygetter.utils.Extensions.secsToTicks
import com.flooferland.waygetter.utils.WaygetterUtils
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.sqrt

class TattleManager(val instance: ITattleInstance) {
    companion object {
        init {
            ServerTickEvents.END_WORLD_TICK.register { level ->
                // Spawning a responsible mama
                run {
                    if (level.gameTime % 10.secsToTicks() != 0L) return@run
                    for (tattletail in level.getEntities(EntityTypeTest.forClass(TattletailEntity::class.java), { _ -> true })) {
                        val searchZone = AABB.ofSize(tattletail.position(), MamaEntity.MAX_DIST, MamaEntity.MAX_DIST, MamaEntity.MAX_DIST)
                        val mamas = level.getEntities(ModEntities.Mama.type, searchZone, { _ -> true })
                        if (mamas.isNotEmpty()) continue

                        val mama = MamaEntity(level)
                        var mamaPos = tattletail.blockPosition()
                        val directions = arrayOf(
                            mamaPos.north(), mamaPos.north().north(),
                            mamaPos.east(), mamaPos.east().east(),
                            mamaPos.south(), mamaPos.south().south(),
                            mamaPos.west(), mamaPos.west().west()
                        )
                        directions.shuffle()
                        for (direction in directions) {
                            if (level.getBlockState(direction).isAir) {
                                mamaPos = direction
                                break
                            }
                        }
                        mama.invisOnSpawn = true
                        mama.setPos(mamaPos.bottomCenter)
                        mama.lookAt(tattletail)
                        level.addFreshEntity(mama)
                    }
                }
            }
        }

        fun getTooDark(level: Level, pos: BlockPos, player: Player?): Boolean {
            val skyLight = level.getBrightness(LightLayer.SKY, pos)
            val blockLight = level.getBrightness(LightLayer.BLOCK, pos)
            val light = ((skyLight + blockLight) + if (level.dayTime in 13000..23000) -15 else 0).absoluteValue
            val tooDark = (light < 12) && (player?.let { player ->
                player.entityData.get(ModSynchedData.flashlightBattery) < 0.1f || !player.isHolding { it.item is FlashlightItem }
            } ?: true)
            return tooDark
        }
    }

    // TODO: Fix client/server BS, send animation to the client when playAnim is called

    fun tick() {
        val state = instance.state
        val level = instance.getLevel() as? ServerLevel ?: return

        // Not ticking if not necessary
        val canYap = run {
            val owner = getOwner() ?: return@run false
            val nearestPlayer = level.getNearestPlayer(owner, 10.0) ?: return@run false
            val clip = level.clip(ClipContext(owner.eyePosition, nearestPlayer.eyePosition, ClipContext.Block.VISUAL, ClipContext.Fluid.ANY, owner))
            clip.type == HitResult.Type.MISS || instance is TattleItemStackInstance
        }

        // Adjusting state
        state.timeIdle++
        if (state.timeIdle > state.nextYapTime) {
            state.timeIdle = 0
            if (canYap) yap(level, state)
        }
        instance.state = state

        // Side effects
        val owner = getOwner()
        if (state.timeIdle < 20 * 2.2f && owner is ServerPlayer) {
            NoiseTracker.add(owner, NoiseTracker.NOISE_MEDIUM)
        }
    }

    fun yap(level: ServerLevel, state: TattleState) {
        val tooDark = getTooDark(level, instance.getPos(), (instance as? TattleItemStackInstance)?.player)
        if (tooDark) {
            if (!state.scared) {
                state.nextYapTime = 3.secsToTicks()
                playAnim("its_dark")
                state.scared = true
            } else {
                state.nextYapTime = 2.secsToTicks()
                playAnim("ahh")
            }
        } else if (state.scared) {
            state.scared = false
            state.nextYapTime = 5.secsToTicks() + WaygetterUtils.random.nextIntBetweenInclusive(1, 3).secsToTicks()
        } else {
            state.nextYapTime = 3.secsToTicks() + WaygetterUtils.random.nextIntBetweenInclusive(5, 20).secsToTicks()
            playRandomIdle()
        }
    }

    fun getOwner() = when (instance) {
        is TattletailEntity if !instance.isRemoved -> instance
        is TattleItemStackInstance -> instance.player
        else -> null
    }

    fun playRandomIdle() {
        val anims = arrayOf(
            "me_tattletail",
            "thats_me"
        )
        val anim = anims.randomOrNull() ?: return
        playAnim(anim)
    }

    fun playAnim(name: String) {
        for (player in instance.getLevel().players()) {
            if (player !is ServerPlayer) continue
            val owner = getOwner()?.uuid ?: continue
            ServerPlayNetworking.send(player, TattleStatePacket(owner = owner, playAnim = name))
        }
    }
}