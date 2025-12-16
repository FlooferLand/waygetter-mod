package com.flooferland.waygetter.registry

import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import com.flooferland.waygetter.WaygetterMod
import com.flooferland.waygetter.utils.rl

enum class ModItemGroups {
    Main("main", { params, out ->
        for (block in ModBlocks.entries) {
            out.accept(block.item)
        }
        for (item in ModItems.entries) {
            out.accept(item.item)
        }
    });

    constructor(name: String, generator: CreativeModeTab.DisplayItemsGenerator) {
        val group = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup.${WaygetterMod.MOD_ID}.$name"))
            .icon({ ModItems.Tattletail.item.defaultInstance })
            .displayItems(generator)
            .build()
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, rl(name), group)
    }
}