package com.flooferland.waygetter.registry

import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import com.flooferland.waygetter.blocks.BrushBlock
import com.flooferland.waygetter.blocks.ChargerBlock
import com.flooferland.waygetter.utils.rl

enum class ModBlocks {
    Charger("charger", ::ChargerBlock),
    Brush("brush", ::BrushBlock)
    ;

    val id: ResourceLocation
    val block: Block
    val item: BlockItem
    constructor(name: String, block: (properties: Properties) -> Block, properties: Properties = Properties.of()) {
        this.id = rl(name)
        this.block = block(properties)
        this.item = BlockItem(this.block, Item.Properties())
        Registry.register(BuiltInRegistries.BLOCK, this.id, this.block)
        Registry.register(BuiltInRegistries.ITEM, this.id, this.item)
    }
}