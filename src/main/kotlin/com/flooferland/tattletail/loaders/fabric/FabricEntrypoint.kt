//? if fabric {
package com.flooferland.tattletail.loaders.fabric

import com.flooferland.tattletail.loaders.TattleMod
import net.fabricmc.api.ModInitializer

public object FabricEntrypoint : ModInitializer {
    override fun onInitialize() {
        TattleMod.initialize()
    }
}
//? }