//? if fabric {
package com.flooferland.waygetter.loaders.fabric

import com.flooferland.waygetter.WaygetterMod
import net.fabricmc.api.ModInitializer

public object FabricEntrypoint : ModInitializer {
    override fun onInitialize() {
        WaygetterMod.initialize()
    }
}
//? }