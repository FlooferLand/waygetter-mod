package com.flooferland.waygetter.utils

import net.minecraft.client.gui.*
import net.minecraft.client.renderer.*

object ClientExtensions {
    fun GuiGraphics.fillGradientHorizontal(x1: Int, y1: Int, x2: Int, y2: Int, colorFrom: Int, colorTo: Int, z: Int = 0) {
        val matrix = pose().last().pose()
        val buffer = bufferSource().getBuffer(RenderType.gui())

        buffer.addVertex(matrix, x1.toFloat(), y1.toFloat(), z.toFloat()).setColor(colorFrom)
        buffer.addVertex(matrix, x1.toFloat(), y2.toFloat(), z.toFloat()).setColor(colorFrom)
        buffer.addVertex(matrix, x2.toFloat(), y2.toFloat(), z.toFloat()).setColor(colorTo)
        buffer.addVertex(matrix, x2.toFloat(), y1.toFloat(), z.toFloat()).setColor(colorTo)
        flush()
    }
}