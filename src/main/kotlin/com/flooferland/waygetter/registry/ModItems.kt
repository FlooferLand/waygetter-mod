package com.flooferland.waygetter.registry

import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.Item.Properties
import com.flooferland.waygetter.utils.rl

enum class ModItems {
    ;

    val id: ResourceLocation
    val item: Item
    constructor(name: String, item: (Properties) -> Item, properties: Properties) {
        this.id = rl(name)
        this.item = item(properties)
        Registry.register(BuiltInRegistries.ITEM, this.id, this.item)
    }
}