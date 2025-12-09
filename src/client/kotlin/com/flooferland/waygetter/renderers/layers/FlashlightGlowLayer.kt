package com.flooferland.waygetter.renderers.layers

import net.minecraft.client.*
import net.minecraft.client.renderer.*
import com.flooferland.waygetter.items.FlashlightItem
import com.flooferland.waygetter.registry.ModSynchedData
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import java.awt.Color
import software.bernie.geckolib.cache.`object`.BakedGeoModel
import software.bernie.geckolib.cache.texture.AutoGlowingTexture
import software.bernie.geckolib.renderer.GeoRenderer
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer

class FlashlightGlowLayer(renderer: GeoRenderer<FlashlightItem>) : AutoGlowingGeoLayer<FlashlightItem>(renderer) {
    override fun getRenderType(animatable: FlashlightItem?, bufferSource: MultiBufferSource?): RenderType? {
        val texture = AutoGlowingTexture.getEmissiveResource(getTextureResource(animatable))
        return RenderType.outline(texture)
    }

    override fun render(poseStack: PoseStack, animatable: FlashlightItem, bakedModel: BakedGeoModel, renderType: RenderType?, bufferSource: MultiBufferSource, buffer: VertexConsumer?, partialTick: Float, packedLight: Int, packedOverlay: Int) {
        val player = Minecraft.getInstance()?.player ?: return

        val renderType = getRenderType(animatable, bufferSource) ?: return
        val baseColor = renderer.getRenderColor(animatable, partialTick, packedLight)

        val glow = (player.entityData.get(ModSynchedData.flashlightBattery) * 1.1f).coerceIn(0f..1f)

        val colour = Color(
            (baseColor.red * glow).toInt(),
            (baseColor.green * glow).toInt(),
            (baseColor.blue * glow).toInt(),
            baseColor.alpha
        ).rgb

        renderer.reRender(
            bakedModel, poseStack, bufferSource, animatable, renderType,
            bufferSource.getBuffer(renderType), partialTick, LightTexture.FULL_SKY, packedOverlay,
            colour
        )
    }
}