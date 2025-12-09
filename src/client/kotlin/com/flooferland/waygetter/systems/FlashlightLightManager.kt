package com.flooferland.waygetter.systems

import net.minecraft.client.*
import net.minecraft.client.player.*
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import com.flooferland.waygetter.items.FlashlightItem
import com.flooferland.waygetter.registry.ModSynchedData
import com.flooferland.waygetter.utils.Extensions.getHeldItem
import foundry.veil.api.client.render.VeilRenderSystem
import foundry.veil.api.client.render.light.data.AreaLightData
import foundry.veil.api.client.render.light.renderer.LightRenderHandle
import foundry.veil.platform.VeilEventPlatform
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin

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
        val battery = player.entityData?.get(ModSynchedData.flashlightBattery) ?: 1f
        val brightness = battery * 2.5f

        var handle = lights[player.id]
        if (handle == null) {
            val light = AreaLightData()
            light.setColor(0.4f, 0.7f, 1f)
            light.setSize(0.3, 0.3)
            light.brightness = brightness
            light.distance = 50f
            light.angle = 30f * Mth.DEG_TO_RAD
            handle = VeilRenderSystem.renderer().lightRenderer.addLight(light)
            lights[player.id] = handle
        }

        val light = handle.lightData
        val cameraType = client.options.cameraType
        val view = when (cameraType) {
            CameraType.FIRST_PERSON -> player.getViewVector(1f)
            else -> Vec3(-sin(Math.toRadians(player.yBodyRot.toDouble())), 0.0, cos(Math.toRadians(player.yBodyRot.toDouble())))
        }
        val basePos = when (cameraType) {
            CameraType.FIRST_PERSON -> client.gameRenderer?.mainCamera?.position ?: player.eyePosition
            else -> player.eyePosition
        }
        val forward = when (cameraType) {
            CameraType.FIRST_PERSON -> Vec3(client.gameRenderer?.mainCamera?.lookVector)
            else -> player.forward
        }

        val nudgePos = 1.3
        val nudgeRot = 0.1
        val right = view.cross(Vec3(0.0, 1.0, 0.0)).normalize()
        val start = basePos.add(right.scale(0.5)).add(forward.scale(if (cameraType == CameraType.FIRST_PERSON) -nudgePos else nudgePos))
        light.position.set(start.toVector3f())
        light.orientation.set(
            Quaternionf().lookAlong(view.add(right.scale(-nudgeRot)).scale(-1.0).toVector3f(), Vector3f(0f, 1f, 0f))
        )
        light.brightness = brightness
        light.angle = (25f * (0.5f + (battery * 0.5f))) * Mth.DEG_TO_RAD
        return true
    }
}