package com.flooferland.waygetter.registry

import com.flooferland.waygetter.packets.DisplayNoisePacket
import com.flooferland.waygetter.packets.TattleStatePacket
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry

object ModPackets {
    fun registerS2C() {
        PayloadTypeRegistry.playS2C().register(TattleStatePacket.type, TattleStatePacket.codec)
        PayloadTypeRegistry.playS2C().register(DisplayNoisePacket.type, DisplayNoisePacket.codec)
    }

    @Environment(EnvType.CLIENT)
    fun registerC2S() {

    }
}
