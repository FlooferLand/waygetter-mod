package com.flooferland.waygetter.components

import com.flooferland.waygetter.systems.tattletail.TattleNeeds
import com.mojang.serialization.Codec

class TattleNeedsDataComponent(val needs: TattleNeeds = TattleNeeds()) {
    companion object {
        val CODEC: Codec<TattleNeedsDataComponent> = TattleNeeds.CODEC.xmap(
            { TattleNeedsDataComponent(it) },
            { it.needs }
        )
    }
}