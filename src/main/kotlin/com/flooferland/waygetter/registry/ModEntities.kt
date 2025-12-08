package com.flooferland.waygetter.registry

import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import com.flooferland.waygetter.entities.MamaEntity
import com.flooferland.waygetter.entities.TattletailEntity
import com.flooferland.waygetter.utils.rl
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry

sealed class ModEntities<E>(name: String, factory: (Level) -> E, size: EntityDimensions, category: MobCategory, attributes: AttributeSupplier.Builder? = null) where E: Entity {
    object Tattletail : ModEntities<TattletailEntity>(
        "tattletail", ::TattletailEntity,
        size = EntityDimensions.fixed(0.45f, 0.55f),
        category = MobCategory.MISC
    )
    object Mama : ModEntities<MamaEntity>(
        "mama", ::MamaEntity,
        size = EntityDimensions.fixed(0.6f, 0.85f),
        category = MobCategory.MONSTER,
        attributes = Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 200.0)
    )

    val id = rl(name)
    val type: EntityType<E> = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        this.id,
        EntityType.Builder.of({ _, level -> factory(level) }, category)
            .sized(size.width, size.height)
            .eyeHeight(size.eyeHeight)
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