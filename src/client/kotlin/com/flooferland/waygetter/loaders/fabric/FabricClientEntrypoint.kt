//? if fabric {
package com.flooferland.waygetter.loaders.fabric

import com.flooferland.waygetter.WaygetterModClient
import net.fabricmc.api.ClientModInitializer

class FabricClientEntrypoint : ClientModInitializer {
    override fun onInitializeClient() {
        WaygetterModClient.initialize()
    }
}
//? }
