package com.flooferland.waygetter.systems.tattletail

import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.*
import net.minecraft.world.item.*
import net.minecraft.world.level.*
import net.minecraft.world.phys.HitResult
import com.flooferland.waygetter.components.TattleStateDataComponent
import com.flooferland.waygetter.entities.TattletailEntity
import com.flooferland.waygetter.items.TattletailItem
import com.flooferland.waygetter.packets.TattleStatePacket
import com.flooferland.waygetter.registry.ModComponents
import com.flooferland.waygetter.registry.ModSounds
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
        val level = instance.level as? ServerLevel ?: return
        val lightLevel = level.getBrightness(LightLayer.BLOCK, instance.pos)

        state.timeIdle++
        if (state.timeIdle > state.nextYapTime) {
            state.timeIdle = 0
            state.nextYapTime = (20 * 4) + (20 * WaygetterUtils.random.nextIntBetweenInclusive(5, 20))
            run {
                val owner = getOwner() ?: return@run
                val nearestPlayer = level.getNearestPlayer(owner, 10.0) ?: return@run
                val clip = level.clip(ClipContext(owner.eyePosition, nearestPlayer.eyePosition, ClipContext.Block.VISUAL, ClipContext.Fluid.ANY, owner))
                if (clip.type == HitResult.Type.MISS) {
                    playRandomIdle()
                }
            }
        }
        instance.state = state
    }

    fun getOwner() = when (instance) {
        is TattletailEntity if !instance.isRemoved -> instance
        is TattleItemStackInstance -> instance.player
        else -> null
    }

    fun playRandomIdle() {
        val anims = arrayOf(
            Pair("me_tattletail", ModSounds.TattleBarkMeTattletail),
            Pair("thats_me", ModSounds.TattleBarkThatsMe)
        )
        val anim = anims.randomOrNull() ?: return
        playAnim(anim.first)
    }

    fun playAnim(name: String) {
        for (player in instance.level.players()) {
            if (player !is ServerPlayer) continue
            val owner = getOwner()?.uuid ?: continue
            ServerPlayNetworking.send(player, TattleStatePacket(owner = owner, playAnim = name))
        }
    }
}