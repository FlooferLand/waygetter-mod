package com.flooferland.waygetter.utils

import net.minecraft.core.*
import net.minecraft.resources.*
import net.minecraft.sounds.*
import net.minecraft.world.entity.player.*
import net.minecraft.world.level.*
import com.flooferland.waygetter.WaygetterMod.MOD_ID
import com.flooferland.waygetter.registry.ModSounds

fun rl(id: String): ResourceLocation =
    ResourceLocation.tryBuild(MOD_ID, id)!!

fun rlVanilla(id: String): ResourceLocation =
    ResourceLocation.fromNamespaceAndPath("minecraft", id)

fun <E> MutableList<E>.copy(): MutableList<E> {
    return ArrayList(this)
}

fun lerp(a: Double, b: Double, t: Double): Double {
    return a * (1.0 - t) + b * t
}
fun lerp(a: Float, b: Float, t: Float): Float {
    return a * (1.0f - t) + b * t
}
fun playerMadeSound(level: Level, player: Player, sound: ModSounds, volume: Float = 1.0f, pitch: Float = 1.0f, source: SoundSource = SoundSource.PLAYERS) {
    playerMadeSound(level, player, sound.event, volume, pitch, source)
}
fun playerMadeSound(level: Level, player: Player, event: SoundEvent, volume: Float = 1.0f, pitch: Float = 1.0f, source: SoundSource = SoundSource.PLAYERS) {
    val pitch = pitch + ((WaygetterUtils.random.nextFloat() - 0.5f) * 0.12f)
    level.playSound(player, player.blockPosition().above(), event, source, volume, pitch)
    player.playNotifySound(event, SoundSource.PLAYERS, volume, pitch)
}
fun blockMadeSound(level: Level, player: Player, sound: ModSounds, volume: Float = 1.0f, pitch: Float = 1.0f, source: SoundSource = SoundSource.PLAYERS) {
    playerMadeSound(level, player, sound.event, volume, pitch, source)
}
fun blockMadeSound(level: Level, pos: BlockPos, event: SoundEvent, volume: Float = 1.0f, pitch: Float = 1.0f, source: SoundSource = SoundSource.BLOCKS, user: Player? = null) {
    val pitch = pitch + ((WaygetterUtils.random.nextFloat() - 0.5f) * 0.12f)
    level.playSound(user, pos, event, source, volume, pitch)
}
