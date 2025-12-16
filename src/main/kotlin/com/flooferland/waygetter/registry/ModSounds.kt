package com.flooferland.waygetter.registry

import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import com.flooferland.waygetter.utils.rl

enum class ModSounds {
    TattleBarkAhh("tattletail.bark.ahh"),
    TattleBarkBrushMe("tattletail.bark.brush_me"),
    TattleBarkGiveMeATreat("tattletail.bark.give_me_a_treat"),
    TattleBarkItsDark("tattletail.bark.its_dark"),
    TattleBarkMeTattletail("tattletail.bark.me_tattletail"),
    TattleBarkMeTired("tattletail.bark.me_tired"),
    TattleBarkNightNight("tattletail.bark.night_night"),
    TattleBarkThatsMe("tattletail.bark.thats_me"),
    TattleBarkUhOh("tattletail.bark.uh_oh"),
    MamaIdle("mama.idle"),
    MamaJumpscare("mama.jumpscare"),
    MamaTaunt("mama.taunt"),
    NoiseBeep("noise_beep"),
    FlashlightShake("flashlight_shake"),
    FlashlightDie("flashlight_die"),
    AlertStinger("alert_stinger"),
    InfoStinger("info_stinger"),
    EggCollect("egg_collect"),
    TensionLoop("tension_loop")
    ;

    val id: ResourceLocation
    val event: SoundEvent
    constructor(name: String) {
        this.id = rl(name)
        this.event = SoundEvent.createVariableRangeEvent(this.id)
        Registry.register(BuiltInRegistries.SOUND_EVENT, this.id, this.event)
    }
}