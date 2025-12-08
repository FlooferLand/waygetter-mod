package com.flooferland.waygetter

import net.minecraft.world.entity.player.Player
import com.flooferland.waygetter.components.TattleStateDataComponent
import com.flooferland.waygetter.entities.MamaEntity
import com.flooferland.waygetter.entities.TattletailEntity
import com.flooferland.waygetter.items.MamaItem
import com.flooferland.waygetter.items.TattletailItem
import com.flooferland.waygetter.items.TattletailClient
import com.flooferland.waygetter.models.MamaModel
import com.flooferland.waygetter.models.TattletailModel
import com.flooferland.waygetter.packets.TattleStatePacket
import com.flooferland.waygetter.registry.ModComponents
import com.flooferland.waygetter.registry.ModEntities
import com.flooferland.waygetter.registry.ModItems
import com.flooferland.waygetter.registry.ModPackets
import com.flooferland.waygetter.renderers.NoiseHudRenderer
import com.flooferland.waygetter.systems.NoiseTrackerClient
import com.flooferland.waygetter.systems.tattletail.TattleState
import com.flooferland.waygetter.utils.Extensions.getHeldItem
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import software.bernie.geckolib.animatable.client.GeoRenderProvider
import software.bernie.geckolib.animation.AnimationController
import software.bernie.geckolib.animation.PlayState
import software.bernie.geckolib.renderer.GeoEntityRenderer
import software.bernie.geckolib.renderer.GeoItemRenderer

object WaygetterModClient {
    public fun initialize() {
        run {
            ModPackets.registerC2S()
            NoiseTrackerClient.init()
            NoiseHudRenderer.init()
        }

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

        // Mama GeckoLib model
        run {
            (ModItems.Mama.item as MamaItem).renderProviderHolder.value = object : GeoRenderProvider {
                var renderer: GeoItemRenderer<*>? = null
                override fun getGeoItemRenderer(): GeoItemRenderer<*>? {
                    if (renderer == null) renderer = object : GeoItemRenderer<MamaItem>(MamaModel()) {}
                    return renderer!!
                }
            }
            EntityRendererRegistry.register(ModEntities.Mama.type) { context ->
                object : GeoEntityRenderer<MamaEntity>(context, MamaModel()) {}
            }
        }

        // GeckoLib again
        TattletailItem.REGISTER_CONTROLLERS = { self, controllers ->
            TattletailClient.registerControllers(self, controllers)
        }
        MamaItem.REGISTER_CONTROLLERS = { self, controllers ->
            controllers.add(AnimationController(self, "main") {
                PlayState.CONTINUE
            })
        }

        // Packets
        // TODO: Move this into its own separate class mirroring the server/common-side one
        ClientPlayNetworking.registerGlobalReceiver(TattleStatePacket.type) { packet, context ->
            val mc = context.client() ?: return@registerGlobalReceiver
            val level = mc.level ?: return@registerGlobalReceiver
            fun setState(state: TattleState) {
                state.currentAnim = packet.playAnim
            }
            val targetId = packet.owner
            val entity = level.entitiesForRendering().firstOrNull { it.uuid == targetId } ?: return@registerGlobalReceiver
            if (entity is Player) {
                val stack = entity.getHeldItem { it.item is TattletailItem } ?: return@registerGlobalReceiver
                val state = TattleState()
                setState(state)
                stack.set(ModComponents.TattleStateData.type, TattleStateDataComponent(state))
            } else if (entity is TattletailEntity) {
                setState(entity.state)
            }
        }
    }
}