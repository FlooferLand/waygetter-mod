package com.flooferland.waygetter.systems.tattletail

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

data class TattleNeeds(
    var feed: Float = 1.0f,
    var groom: Float = 1.0f,
    var battery: Float = 1.0f
) {
    companion object {
        val CODEC: Codec<TattleNeeds> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.FLOAT.fieldOf("hunger").forGetter { it.feed },
                Codec.FLOAT.fieldOf("groom").forGetter { it.groom },
                Codec.FLOAT.fieldOf("battery").forGetter { it.battery }
            ).apply(instance) { hunger, groom, battery ->
                TattleNeeds(hunger, groom, battery)
            }
        }
    }
}