package com.flooferland.waygetter.datagen

import com.flooferland.waygetter.datagen.providers.*
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

class WaygetterDataGenerator : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(generator: FabricDataGenerator) {
        val pack = generator.createPack()!!
        pack.addProvider(::BlockProvider)
        pack.addProvider(::LootTableProvider)
        pack.addProvider(::RecipeProvider)
    }
}