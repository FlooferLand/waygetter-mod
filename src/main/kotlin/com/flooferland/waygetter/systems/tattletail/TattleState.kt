package com.flooferland.waygetter.systems.tattletail

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import com.flooferland.waygetter.utils.Extensions.getBooleanOrNull
import com.flooferland.waygetter.utils.Extensions.getIntOrNull
import com.flooferland.waygetter.utils.Extensions.getStringOrNull
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

data class TattleState(
    var timeIdle: Int = 0,
    var nextYapTime: Int = 0,
    var lookDir: Byte = 0,
    var scared: Boolean = false,
    var tired: Boolean = false,
    var currentAnim: String = "",
) {
    companion object {
        val CODEC: Codec<TattleState> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.INT.optionalFieldOf("timeIdle", 0).forGetter { it.timeIdle },
                Codec.INT.optionalFieldOf("nextYapTime", 0).forGetter { it.nextYapTime },
                Codec.BYTE.optionalFieldOf("lookDir", 0).forGetter { it.lookDir },
                Codec.BOOL.optionalFieldOf("scared", false).forGetter { it.scared },
                Codec.BOOL.optionalFieldOf("tired", false).forGetter { it.scared },
                Codec.STRING.optionalFieldOf("currentAnim", "").forGetter { it.currentAnim },
            ).apply(instance) { timeIdle, nextYapTime, lookDir, scared, tired, currentAnim ->
                TattleState(timeIdle, nextYapTime, lookDir, scared, tired, currentAnim)
            }
        }
        fun load(tag: CompoundTag) = runCatching { CODEC.decode(NbtOps.INSTANCE, tag).orThrow }.getOrNull()?.first
    }
    fun save() = runCatching { CODEC.encodeStart(NbtOps.INSTANCE, this).orThrow }.getOrNull()
}