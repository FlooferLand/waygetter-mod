package com.flooferland.waygetter.renderers

import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.util.FastColor
import com.flooferland.waygetter.components.TattleNeedsDataComponent
import com.flooferland.waygetter.items.TattletailItem
import com.flooferland.waygetter.registry.ModComponents
import com.flooferland.waygetter.renderers.TattleNeedHudRenderer.NeedId.*
import com.flooferland.waygetter.systems.tattletail.TattleManager
import com.flooferland.waygetter.utils.ClientExtensions.fillGradientHorizontal
import com.flooferland.waygetter.utils.Extensions.getHeldItem
import com.flooferland.waygetter.utils.WaygetterUtils
import com.flooferland.waygetter.utils.lerp
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback

// TODO: [Config] Add a way to change the position of the Tattletail statistics HUD

object TattleNeedHudRenderer {
    private data class Need(val id: NeedId, val name: String, val color: Int, var value: Float = 1f)
    private enum class NeedId { Battery, Feed, Groom }

    private val needs = arrayOf(
        Need(Battery, "BATT.", FastColor.ARGB32.color(185, 220, 115)),
        Need(Feed, "FEED",  FastColor.ARGB32.color(255, 150, 70)),
        Need(Groom, "GROOM", FastColor.ARGB32.color(125, 120, 230))
    )

    fun init() {
        HudRenderCallback.EVENT.register { graphics, delta ->
            val client = Minecraft.getInstance()
            val player = client.player ?: return@register
            if (!player.isHolding { it.item is TattletailItem }) return@register

            render(graphics, delta, graphics.guiWidth(), graphics.guiHeight())
        }
    }

    private fun render(graphics: GuiGraphics, delta: DeltaTracker, clientWidth: Int, clientHeight: Int) {
        val level = Minecraft.getInstance()?.level ?: return
        val player = Minecraft.getInstance()?.player ?: return
        val font = Minecraft.getInstance()?.font ?: return
        val tattleStack = player.getHeldItem { it.item is TattletailItem } ?: return
        val tattleNeeds = tattleStack.components.getOrDefault(ModComponents.TattleNeedsData.type, TattleNeedsDataComponent())

        // Drawing the bars
        val pad = 30
        val sep = 13
        val barWidth = 50
        val barStart = 25
        val fontHeight = font.lineHeight
        for ((i, need) in needs.withIndex()) {
            val lineY = pad + (sep * i)

            // Name
            graphics.drawString(
                font, Component.literal(need.name),
                pad, lineY,
                FastColor.ARGB32.color(255, 255, 255),
                true
            )

            // Getting the need value
            val value = when (need.id) {
                Battery -> tattleNeeds.needs.battery
                Feed -> tattleNeeds.needs.feed
                Groom -> tattleNeeds.needs.groom
            }.coerceIn(0f..1f)
            need.value = lerp(need.value, value, 0.4f * delta.gameTimeDeltaTicks)

            // Bar
            val barOutline = 1
            graphics.fill(  // Backdrop
                pad + sep + barStart - barOutline,
                lineY - barOutline,
                pad + sep + barStart + barWidth + barOutline,
                lineY + fontHeight - 3 + barOutline,
                FastColor.ARGB32.color(110, 0, 0, 0)
            )
            graphics.fillGradientHorizontal(  // Value
                pad + sep + barStart,
                lineY,
                pad + sep + barStart + (need.value * barWidth).toInt(),
                lineY + fontHeight - 3,
                FastColor.ARGB32.multiply(need.color, FastColor.ARGB32.color(140, 140, 140)),
                need.color
            )
        }

        // Low light indicator
        val lowLightPad = 10
        if (TattleManager.getTooDark(level, player.blockPosition(), player)) {
            val randX = (WaygetterUtils.random.nextFloat() * 1.3f).toInt()
            val randY = (WaygetterUtils.random.nextFloat() * 1.3f).toInt()
            val x = pad + sep + barStart + barWidth + lowLightPad + randX
            val y = (sep * needs.size) + lowLightPad + randY
            for ((i, text) in arrayOf("LOW", "LIGHT").withIndex()) {
                graphics.drawString(
                    font, text,
                    x, y + (i * fontHeight),
                    FastColor.ARGB32.color(60, 255, 255, 255),
                    false
                )
            }
        }
    }
}