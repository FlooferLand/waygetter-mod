package com.flooferland.waygetter.systems

import net.minecraft.client.*
import net.minecraft.client.player.*
import net.minecraft.util.Mth
import com.flooferland.waygetter.items.FlashlightItem
import com.flooferland.waygetter.registry.ModSynchedData
import com.flooferland.waygetter.utils.Extensions.getHeldItem
import foundry.veil.api.client.render.VeilRenderSystem
import foundry.veil.api.client.render.light.data.AreaLightData
import foundry.veil.api.client.render.light.renderer.LightRenderHandle
import foundry.veil.platform.VeilEventPlatform
import org.joml.Quaternionf
import org.joml.Vector3f

object FlashlightLightManager {
    val lights = hashMapOf<Int, LightRenderHandle<AreaLightData>>()

    fun init() {
        VeilEventPlatform.INSTANCE.onVeilRenderLevelStage { stage, renderer, source, stack, fc, fc2, i, tracker, camera, frustum ->
            val level = Minecraft.getInstance()?.level ?: return@onVeilRenderLevelStage
            for (player in level.players()) {
                val keepLight = updatePlayer(player, tracker)
                if (!keepLight) {
                    val handle = lights[player.id] ?: continue
                    handle.close()
                    lights.remove(player.id)
                }
            }

            // Removing leftover lights
            if (lights.isNotEmpty()) {
                val current = level.players().associateBy { it.id }
                lights.entries.removeAll { (playerId, handle) ->
                    if (!current.containsKey(playerId)) {
                        handle.close()
                        true
                    } else {
                        false
                    }
                }
            }
        }
    }

    fun updatePlayer(player: AbstractClientPlayer, tracker: DeltaTracker): Boolean {
        val stack = player.getHeldItem { it.item is FlashlightItem } ?: return false
        val client = Minecraft.getInstance() ?: return false
        val brightness = (player.entityData?.get(ModSynchedData.flashlightBattery) ?: 1f) * 3f

        var handle = lights[player.id]
        if (handle == null) {
            val light = AreaLightData()
            light.setColor(0.5f, 0.8f, 1f)
            light.setSize(0.3, 0.3)
            light.brightness = brightness
            light.distance = 100f
            light.angle = 70f * Mth.DEG_TO_RAD
            handle = VeilRenderSystem.renderer().lightRenderer.addLight(light)
            lights[player.id] = handle
        }

        val light = handle.lightData
        val start = when (client.options.cameraType) {
            CameraType.FIRST_PERSON -> client.gameRenderer?.mainCamera?.position ?: player.eyePosition
            else -> player.eyePosition
        }
        light.position.set(start.toVector3f())
        light.orientation.set(Quaternionf().lookAlong(player.getViewVector(1f).scale(-1.0).toVector3f(), Vector3f(0f, 1f, 0f)))
        light.brightness = brightness
        return true
    }
}