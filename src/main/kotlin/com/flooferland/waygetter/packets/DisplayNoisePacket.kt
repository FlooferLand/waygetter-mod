package com.flooferland.waygetter.packets

import net.minecraft.network.*
import net.minecraft.network.codec.*
import net.minecraft.network.protocol.common.custom.*
import com.flooferland.waygetter.utils.rl

/** Sent from the server to the client to notify it that the [com.flooferland.waygetter.systems.NoiseTracker] tracked a noise it made */
class DisplayNoisePacket(val loudness: Float = 0.0f) : CustomPacketPayload {
    override fun type() = type

    companion object {
        val type = CustomPacketPayload.Type<DisplayNoisePacket>(rl("noise_state"))
        val codec = StreamCodec.of<FriendlyByteBuf, DisplayNoisePacket>(
            { buf, chunk ->
                buf.writeFloat(chunk.loudness)
            },
            { buf ->
                val loudness = buf.readFloat()
                DisplayNoisePacket(
                    loudness = loudness
                )
            }
        )!!
    }
}