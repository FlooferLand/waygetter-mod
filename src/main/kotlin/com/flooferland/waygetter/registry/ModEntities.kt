package com.flooferland.waygetter.registry

import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.level.Level
import com.flooferland.waygetter.entities.TattletailEntity
import com.flooferland.waygetter.utils.rl

sealed class ModEntities<E: Entity>(name: String, factory: (Level) -> E) {
    object Tattletail : ModEntities<TattletailEntity>("tattletail", ::TattletailEntity)

    val id = rl(name)
    val type: EntityType<E> = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        this.id,
        EntityType.Builder.of({ _, level -> factory(level) }, MobCategory.MISC)
            .sized(0.4f, 0.55f)
            .build()
    )

    companion object {
        init {
            ModEntities::class.sealedSubclasses.forEach { it.objectInstance }
        }
    }
}