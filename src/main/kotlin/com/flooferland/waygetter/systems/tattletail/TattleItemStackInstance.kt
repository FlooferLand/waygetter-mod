package com.flooferland.waygetter.systems.tattletail

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import com.flooferland.waygetter.components.TattleStateDataComponent
import com.flooferland.waygetter.packets.TattleStatePacket
import com.flooferland.waygetter.registry.ModComponents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

class TattleItemStackInstance(val stack: ItemStack, val player: ServerPlayer) : ITattleInstance {
    override val manager = TattleManager(this)
    override var state: TattleState
        get() {
            val comp = stack.get(ModComponents.TattleStateData.type)
            return comp?.state ?: TattleState()
        }
        set(value) {
            stack.set(ModComponents.TattleStateData.type, TattleStateDataComponent(value))
        }
    override val level: Level = player.level()
    override val pos: BlockPos = player.blockPosition()
}