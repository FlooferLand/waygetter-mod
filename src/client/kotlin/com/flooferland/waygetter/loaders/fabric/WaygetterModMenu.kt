//? if fabric {
package com.flooferland.waygetter.loaders.fabric

import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi

class WaygetterModMenu : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { parent -> this.makeScreen(parent) }
    }

    private fun makeScreen(parent: Screen): Screen {
        return object : Screen(Component.literal("Tattletail")) {
        }

        // TODO: Set up Cloth Config
        /*val builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.literal("Tattletail"))
            .setSavingRunnable({ this.onSave() })
        return builder.build()*/
    }
} //? }