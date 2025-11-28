package com.flooferland.waygetter.systems.tattletail

import net.minecraft.sounds.*
import net.minecraft.world.item.*
import net.minecraft.world.level.*
import com.flooferland.waygetter.components.TattleStateDataComponent
import com.flooferland.waygetter.items.TattletailItem
import com.flooferland.waygetter.registry.ModComponents
import com.flooferland.waygetter.registry.ModSounds
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents

object TattleManager {
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
                TattleManager.tick(instance)
                stack.set(ModComponents.TattleStateData.type, TattleStateDataComponent(instance.state))
            }
        }
    }

    // TODO: Fix client/server BS, send animation to the client when playAnim is called

    fun tick(instance: ITattleInstance) {
        val state = instance.state
        val level = instance.level
        val lightLevel = level.getBrightness(LightLayer.BLOCK, instance.pos)

        state.timeIdle++
        if (state.timeIdle > 20 * 4) {
            state.timeIdle = 0
            playRandomIdle(instance, state, level)
        }
        instance.state = state
    }

    fun playRandomIdle(instance: ITattleInstance, state: TattleState, level: Level) {
        val anims = arrayOf(
            Pair("me_tattletail", ModSounds.TattleBarkMeTattletail),
            Pair("thats_me", ModSounds.TattleBarkThatsMe)
        )
        val anim = anims.randomOrNull() ?: return
        instance.playAnim(anim.first)
    }
}