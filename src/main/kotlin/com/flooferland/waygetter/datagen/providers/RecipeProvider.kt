package com.flooferland.waygetter.datagen.providers

import net.minecraft.core.*
import net.minecraft.data.recipes.*
import net.minecraft.world.item.*
import com.flooferland.waygetter.registry.ModBlocks
import com.flooferland.waygetter.registry.ModItems
import java.util.concurrent.CompletableFuture
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider

class RecipeProvider(output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider>) : FabricRecipeProvider(output, registriesFuture) {
    override fun buildRecipes(exporter: RecipeOutput) {
        // Brush
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.Brush.item)
            .define('_', Items.BLACK_CARPET)
            .define('o', Items.CLAY_BALL)
            .define('/', Items.STICK)
            .pattern("_o/")
            .unlockedBy("has_stick", has(Items.STICK))
            .unlockedBy("has_tattletail", has(ModItems.Tattletail.item))
            .save(exporter)

        // Charger
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.Charger.item)
            .define('i', Items.LIGHTNING_ROD)
            .define('o', Items.CLAY_BALL)
            .define('g', Items.GOLD_INGOT)
            .define('r', Items.REDSTONE)
            .pattern("  i")
            .pattern("ogo")
            .pattern("oro")
            .unlockedBy("has_redstone", has(Items.REDSTONE))
            .unlockedBy("has_clay", has(Items.CLAY_BALL))
            .unlockedBy("has_gold", has(Items.GOLD_INGOT))
            .unlockedBy("has_tattletail", has(ModItems.Tattletail.item))
            .save(exporter)

        // Flashlight
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.Flashlight.item)
            .define('P', Items.GLASS_PANE)
            .define('n', Items.IRON_NUGGET)
            .define('C', Items.COPPER_INGOT)
            .pattern("P")
            .pattern("n")
            .pattern("C")
            .unlockedBy("has_glass", has(Items.GLASS))
            .unlockedBy("has_glass_pane", has(Items.GLASS_PANE))
            .unlockedBy("has_iron", has(Items.IRON_INGOT))
            .unlockedBy("has_iron_nugget", has(Items.IRON_NUGGET))
            .unlockedBy("has_copper", has(Items.COPPER_INGOT))
            .unlockedBy("has_tattletail", has(ModItems.Tattletail.item))
            .save(exporter)

        // Tattletail
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.Tattletail.item)
            .define('#', Items.PURPLE_WOOL)
            .define('A', Items.AMETHYST_SHARD)
            .define('J', Items.JUKEBOX)
            .define('G', Items.GOLD_NUGGET)
            .define('R', Items.REDSTONE)
            .pattern("#A#")
            .pattern("#J#")
            .pattern("GRG")
            .unlockedBy("has_wool", has(Items.PURPLE_WOOL))
            .unlockedBy("has_amethyst", has(Items.AMETHYST_SHARD))
            .unlockedBy("has_diamond", has(Items.DIAMOND))
            .unlockedBy("has_jukebox", has(Items.JUKEBOX))
            .unlockedBy("has_nugget", has(Items.GOLD_NUGGET))
            .unlockedBy("has_redstone", has(Items.REDSTONE))
            .unlockedBy("has_flashlight", has(ModItems.Flashlight.item))
            .save(exporter)
    }
}