package com.flooferland.waygetter.registry

import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.level.Level
import com.flooferland.waygetter.entities.MamaEntity
import com.flooferland.waygetter.entities.TattletailEntity
import com.flooferland.waygetter.utils.rl
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry

sealed class ModEntities<E: Entity>(name: String, factory: (Level) -> E, attributes: AttributeSupplier.Builder? = null) {
    object Tattletail : ModEntities<TattletailEntity>("tattletail", ::TattletailEntity)
    object Mama : ModEntities<MamaEntity>("mama", ::MamaEntity, attributes = Monster.createMonsterAttributes())

    val id = rl(name)
    val type: EntityType<E> = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        this.id,
        EntityType.Builder.of({ _, level -> factory(level) }, MobCategory.MISC)
            .sized(0.4f, 0.55f)
            .build()
    )

    init {
        if (attributes != null) {
            @Suppress("UNCHECKED_CAST")
            (type as? EntityType<LivingEntity>)?.let {
                FabricDefaultAttributeRegistry.register(type, attributes)
            }
        }
    }

    companion object {
        init {
            ModEntities::class.sealedSubclasses.forEach { it.objectInstance }
        }
    }
}