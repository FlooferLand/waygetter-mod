package com.flooferland.waygetter.registry

import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.item.Item
import net.minecraft.world.item.Item.Properties
import com.flooferland.waygetter.utils.rl

enum class ModSounds {
    TattleBarkMeTattletail("tattletail.bark.me_tattletail"),
    TattleBarkThatsMe("tattletail.bark.thats_me")
    ;

    val id: ResourceLocation
    val event: SoundEvent
    constructor(name: String) {
        this.id = rl(name)
        this.event = SoundEvent.createVariableRangeEvent(this.id)
        Registry.register(BuiltInRegistries.SOUND_EVENT, this.id, this.event)
    }
}