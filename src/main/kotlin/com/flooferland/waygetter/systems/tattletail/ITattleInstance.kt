package com.flooferland.waygetter.systems.tattletail

import net.minecraft.core.BlockPos
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

interface ITattleInstance {
    val manager: TattleManager
    fun getTattleStack(): ItemStack
    fun getLevel(): Level
    fun getPos(): BlockPos
}