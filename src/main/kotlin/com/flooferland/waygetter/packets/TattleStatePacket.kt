package com.flooferland.waygetter.packets

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.codec.StreamEncoder
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import com.flooferland.waygetter.utils.rl
import java.util.UUID

data class TattleStatePacket(val owner: UUID?, val playAnim: String = "") : CustomPacketPayload {
    override fun type() = type

    companion object {
        val type = CustomPacketPayload.Type<TattleStatePacket>(rl("tattle_state"))
        val codec = StreamCodec.of<FriendlyByteBuf, TattleStatePacket>(
            { buf, chunk ->
                buf.writeNullable(chunk.owner, StreamEncoder { buff, uuid -> buff.writeUUID(uuid) })
                buf.writeUtf(chunk.playAnim)
            },
            { buf ->
                val ownerPlayer = buf.readNullable { byteBuf -> byteBuf.readUUID() }
                val playAnim = buf.readUtf()
                TattleStatePacket(
                    owner = ownerPlayer,
                    playAnim = playAnim
                )
            }
        )!!
    }
}