package com.flooferland.waygetter.registry

import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.Item.Properties
import com.flooferland.waygetter.items.MamaItem
import com.flooferland.waygetter.items.TattletailItem
import com.flooferland.waygetter.utils.rl

enum class ModItems {
    Tattletail("tattletail", ::TattletailItem),
    Mama("mama", ::MamaItem)
    ;

    val id: ResourceLocation
    val item: Item
    constructor(name: String, item: (Properties) -> Item, properties: Properties = Properties()) {
        this.id = rl(name)
        this.item = item(properties)
        Registry.register(BuiltInRegistries.ITEM, this.id, this.item)
    }
}