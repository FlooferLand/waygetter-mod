package com.flooferland.waygetter.registry

import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.resources.ResourceLocation
import com.flooferland.waygetter.components.TattleNeedsDataComponent
import com.flooferland.waygetter.components.TattleStateDataComponent
import com.flooferland.waygetter.utils.rl

sealed class ModComponents<T> {
    data object TattleStateData : ModComponents<TattleStateDataComponent>(
        "tattle_state",
        { it
            .persistent(TattleStateDataComponent.CODEC)
            .networkSynchronized(ByteBufCodecs.fromCodec(TattleStateDataComponent.CODEC))
        }
    )
    data object TattleNeedsData : ModComponents<TattleNeedsDataComponent>(
        "tattle_needs",
        { it
            .persistent(TattleNeedsDataComponent.CODEC)
        }
    )

    constructor(name: String, builder: (DataComponentType.Builder<T>) -> DataComponentType.Builder<T>) {
        this.id = rl(name)
        this.type = builder(DataComponentType.builder()).build()
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, this.id, this.type)
    }

    val id: ResourceLocation
    val type: DataComponentType<T>

    companion object {
        init {
            ModComponents::class.sealedSubclasses.forEach { it.objectInstance }
        }
    }
}