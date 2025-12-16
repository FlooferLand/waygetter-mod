package com.flooferland.waygetter.systems

import net.minecraft.core.Holder
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.level.gameevent.vibrations.VibrationInfo
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem
import com.flooferland.waygetter.packets.DisplayNoisePacket
import java.util.WeakHashMap
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

/** Server-side tracker to handle noise management */
object NoiseTracker {
    const val MAX_NOISE = 1f
    const val NOISE_SILENT = 0.05f
    const val NOISE_QUIET = 0.1f
    const val NOISE_MEDIUM = 0.4f
    const val NOISE_LOUD = 0.8f
    const val DECAY = 0.03f // per tick

    private val noiseMap = WeakHashMap<ServerPlayer, Float>()
    private val lastingNoiseMap = WeakHashMap<ServerPlayer, MutableList<LastingNoise>>()

    private data class LastingNoise(val amount: Float = 0f, var ticksLeft: Int = 0)

    //region Management
    fun get(player: ServerPlayer): Float {
        val lasting = (lastingNoiseMap[player] ?: mutableListOf()).asSequence().map { it.amount }.sum()
        return noiseMap.getOrDefault(player, 0f) + lasting
    }

    fun set(player: ServerPlayer, amount: Float) {
        noiseMap[player] = amount.coerceIn(0f..MAX_NOISE)
    }

    fun add(player: ServerPlayer, amount: Float) {
        set(player, get(player) + amount)
    }

    fun addLasting(player: ServerPlayer, amount: Float, ticks: Int) {
        val map = lastingNoiseMap.getOrPut(player) { mutableListOf() }
        map.add(LastingNoise(amount, ticks))
    }

    fun updateVibration(player: ServerPlayer, event: Holder<GameEvent>) {
        val type = event.unwrapKey().get().location().path

        var value = when (type) {
            "block_place" -> NOISE_MEDIUM * 1.1f
            "block_destroy" -> NOISE_MEDIUM * 1.3f
            "block_open", "block_close" -> NOISE_MEDIUM * 1.2f
            "hit_ground" -> NOISE_MEDIUM * 1.3f
            "step" -> NOISE_MEDIUM * 1.3f
            else -> NOISE_MEDIUM
        }
        if (player.isSprinting) value *= 2f
        if (player.isCrouching) value /= 2f
        add(player, value)
    }
    // endregion

    private fun updatePlayer(player: ServerPlayer) {
        set(player, get(player) - DECAY)
    }

    /** Registers the global player events */
    fun register() {
        ServerTickEvents.START_WORLD_TICK.register { level ->
            for (player in level.players()) {
                if (player == null) continue
                updatePlayer(player)

                // Lasting noise
                lastingNoiseMap[player]?.let { lasting ->
                    lasting.removeIf {
                        it.ticksLeft -= 1
                        it.ticksLeft <= 0
                    }
                }

                // TODO: [Speed] Figure out a way to not send this every frame
                ServerPlayNetworking.send(player, DisplayNoisePacket(get(player)))
            }
        }
    }
}