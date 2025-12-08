package com.flooferland.waygetter.renderers

import net.minecraft.client.*
import net.minecraft.client.gui.*
import com.flooferland.waygetter.items.TattletailItem
import com.flooferland.waygetter.systems.NoiseTrackerClient
import com.flooferland.waygetter.utils.WaygetterUtils
import com.flooferland.waygetter.utils.rl
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.math.Axis
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback

// TODO: [Config] Add a way to change the position of the speaker

object NoiseHudRenderer {
    fun init() {
        HudRenderCallback.EVENT.register { graphics, delta ->
            val client = Minecraft.getInstance()
            val player = client.player ?: return@register
            if (client.screen != null) return@register
            if (!player.isHolding { it.item is TattletailItem }) return@register

            render(graphics, graphics.guiWidth(), graphics.guiHeight())
        }
    }

    fun render(graphics: GuiGraphics, clientWidth: Int, clientHeight: Int) {
        val noise = NoiseTrackerClient.localPlayerNoise
        if (noise <= 0) return

        val pad = 10
        val size = 64
        val sizeMul = (1f + (noise * 15f)).toInt()
        val rotation = (WaygetterUtils.random.nextDouble() * 20.0) * noise
        val center = pad + (size + sizeMul) / 2.0

        graphics.pose().pushPose()
        RenderSystem.enableBlend()
        graphics.pose().translate(center, center, 0.0)
        graphics.pose().mulPose(Axis.ZP.rotationDegrees(rotation.toFloat()))
        graphics.pose().translate(-center, -center, 0.0)
        graphics.setColor(1f, 1f, 1f, noise)
        graphics.blit(
            rl("textures/gui/noise_icon.png"),
            pad - (sizeMul / 2), pad - (sizeMul / 2),
            size + sizeMul, size + sizeMul,
            0f, 0f,
            size, size,
            size, size
        )
        graphics.setColor(1f, 1f, 1f, 1f)
        RenderSystem.defaultBlendFunc()
        graphics.pose().popPose()
    }
}