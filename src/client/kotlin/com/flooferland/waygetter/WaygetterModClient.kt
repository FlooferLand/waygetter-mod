package com.flooferland.waygetter

import com.flooferland.waygetter.components.TattleStateDataComponent
import com.flooferland.waygetter.entities.TattletailEntity
import com.flooferland.waygetter.items.TattletailItem
import com.flooferland.waygetter.items.TattletailClient
import com.flooferland.waygetter.models.TattletailModel
import com.flooferland.waygetter.packets.TattleStatePacket
import com.flooferland.waygetter.registry.ModComponents
import com.flooferland.waygetter.registry.ModEntities
import com.flooferland.waygetter.registry.ModItems
import com.flooferland.waygetter.registry.ModPackets
import com.flooferland.waygetter.systems.tattletail.TattleState
import com.flooferland.waygetter.utils.Extensions.getHeldItem
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import software.bernie.geckolib.animatable.client.GeoRenderProvider
import software.bernie.geckolib.renderer.GeoEntityRenderer
import software.bernie.geckolib.renderer.GeoItemRenderer

object WaygetterModClient {
    public fun initialize() {
        // Tattletail GeckoLib model
        run {
            (ModItems.Tattletail.item as TattletailItem).renderProviderHolder.value = object : GeoRenderProvider {
                var renderer: GeoItemRenderer<*>? = null
                override fun getGeoItemRenderer(): GeoItemRenderer<*>? {
                    if (renderer == null) renderer = object : GeoItemRenderer<TattletailItem>(TattletailModel()) {}
                    return renderer!!
                }
            }
            EntityRendererRegistry.register(ModEntities.Tattletail.type) { context ->
                object : GeoEntityRenderer<TattletailEntity>(context, TattletailModel()) {}
            }
        }

        // GeckoLib again
        TattletailItem.REGISTER_CONTROLLERS = { self, controllers ->
            TattletailClient.registerControllers(self, controllers)
        }

        // Registers
        ModPackets.registerC2S()

        // Packets
        ClientPlayNetworking.registerGlobalReceiver(TattleStatePacket.type) { packet, context ->
            val player = context.player() ?: return@registerGlobalReceiver
            val target = player.level().getPlayerByUUID(packet.ownerPlayer) ?: return@registerGlobalReceiver
            val stack = target.getHeldItem { it.item is TattletailItem } ?: return@registerGlobalReceiver
            val state = TattleState()
            state.currentAnim = packet.playAnim
            stack.set(ModComponents.TattleStateData.type, TattleStateDataComponent(state))
        }
    }
}