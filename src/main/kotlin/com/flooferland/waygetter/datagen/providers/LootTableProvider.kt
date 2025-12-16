package com.flooferland.waygetter.datagen.providers

import net.minecraft.core.HolderLookup
import com.flooferland.waygetter.registry.ModBlocks
import java.util.concurrent.CompletableFuture
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider

class LootTableProvider(dataOutput: FabricDataOutput, registryLookup: CompletableFuture<HolderLookup.Provider>) : FabricBlockLootTableProvider(dataOutput, registryLookup) {
    override fun generate() {
        for (entry in ModBlocks.entries) {
            dropSelf(entry.block)
        }
    }
}