package com.flooferland.waygetter.components

import net.minecraft.network.codec.StreamCodec
import com.flooferland.waygetter.systems.tattletail.TattleState
import com.mojang.serialization.Codec

data class TattleStateDataComponent(var state: TattleState) {
    companion object {
        val CODEC: Codec<TattleStateDataComponent> = TattleState.CODEC.xmap(
            { TattleStateDataComponent(it) },
            { it.state }
        )
    }
}