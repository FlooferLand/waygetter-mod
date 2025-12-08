package com.flooferland.waygetter.systems

import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import com.flooferland.waygetter.items.FlashlightItem
import com.flooferland.waygetter.packets.DisplayNoisePacket
import com.flooferland.waygetter.registry.ModSounds
import com.flooferland.waygetter.utils.Extensions.canMakeSound
import com.flooferland.waygetter.utils.Extensions.isProvokingMama
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

object NoiseTrackerClient {
    private var lastNoise = 0f
    var localPlayerNoise = 0f

    fun init() {
        val client = Minecraft.getInstance() ?: return

        ClientPlayNetworking.registerGlobalReceiver(DisplayNoisePacket.type) { packet, context ->
            localPlayerNoise = packet.loudness
        }

        // Play the *BEEP* *BEEP*
        ClientTickEvents.END_WORLD_TICK.register { level ->
            if (localPlayerNoise <= NoiseTracker.NOISE_SILENT) return@register
            val player = client.player ?: return@register
            if (!player.canMakeSound()) return@register

            if (localPlayerNoise < NoiseTracker.NOISE_LOUD) {
                lastNoise /= 2
            } else {
                lastNoise += 1
            }
            lastNoise = lastNoise.coerceAtLeast(0f)

            val time = when {
                player.isSprinting || lastNoise > 15 -> 5
                else -> 13
            }

            if (level.gameTime % time == 0L) {
                val volume = localPlayerNoise
                client.soundManager.play(SimpleSoundInstance.forUI(ModSounds.NoiseBeep.event, 1.0f, volume))
            }
        }
    }
}