package com.flooferland.waygetter.systems.tattletail

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import com.flooferland.waygetter.components.TattleNeedsDataComponent
import com.flooferland.waygetter.components.TattleStateDataComponent
import com.flooferland.waygetter.packets.TattleStatePacket
import com.flooferland.waygetter.registry.ModComponents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

class TattleItemStackInstance(val stack: ItemStack, val player: ServerPlayer) : ITattleInstance {
    override val manager = TattleManager(this)
    override fun getTattleStack() = stack
    override fun getLevel() = player.level()!!
    override fun getPos() = player.blockPosition()!!
}