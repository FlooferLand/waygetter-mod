package com.flooferland.waygetter.systems.tattletail

import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level

interface ITattleInstance {
    var state: TattleState
    val level: Level
    val pos: BlockPos
    fun playAnim(name: String)
}