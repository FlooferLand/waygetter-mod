package com.flooferland.waygetter.systems.tattletail

import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.*
import net.minecraft.world.item.*
import net.minecraft.world.level.*
import net.minecraft.world.phys.HitResult
import com.flooferland.waygetter.components.TattleStateDataComponent
import com.flooferland.waygetter.entities.TattletailEntity
import com.flooferland.waygetter.items.FlashlightItem
import com.flooferland.waygetter.items.TattletailItem
import com.flooferland.waygetter.packets.TattleStatePacket
import com.flooferland.waygetter.registry.ModComponents
import com.flooferland.waygetter.registry.ModSounds
import com.flooferland.waygetter.registry.ModSynchedData
import com.flooferland.waygetter.systems.NoiseTracker
import com.flooferland.waygetter.utils.Extensions.secsToTicks
import com.flooferland.waygetter.utils.WaygetterUtils
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

class TattleManager(val instance: ITattleInstance) {
    companion object {
        init {
            ServerTickEvents.END_WORLD_TICK.register { level ->
                for (player in level.players()) {
                    var stack: ItemStack? = null
                    if (player.mainHandItem.item is TattletailItem)
                        stack = player.mainHandItem
                    if (player.offhandItem.item is TattletailItem)
                        stack = player.offhandItem
                    if (stack == null) continue
                    val instance = TattleItemStackInstance(stack, player)
                    instance.manager.tick()
                    stack.set(ModComponents.TattleStateData.type, TattleStateDataComponent(instance.state))
                }
            }
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
        val light = level.getBrightness(LightLayer.SKY, instance.getPos()) + level.getBrightness(LightLayer.BLOCK, instance.getPos())
        println(light)
        val tooDark = (light < 15)
                && ((instance as? TattleItemStackInstance)?.player?.let { player ->
                    player.entityData.get(ModSynchedData.flashlightBattery) < 0.1f || !player.isHolding { it.item is FlashlightItem }
                } ?: true)

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