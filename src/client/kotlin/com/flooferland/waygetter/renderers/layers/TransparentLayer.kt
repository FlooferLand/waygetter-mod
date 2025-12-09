package com.flooferland.waygetter.renderers.layers

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import com.flooferland.waygetter.items.FlashlightItem
import com.flooferland.waygetter.registry.ModSynchedData
import com.flooferland.waygetter.renderers.FlashlightRenderer
import com.flooferland.waygetter.utils.rl
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import software.bernie.geckolib.cache.`object`.BakedGeoModel
import software.bernie.geckolib.renderer.layer.GeoRenderLayer
import software.bernie.geckolib.util.Color

class TransparentLayer(renderer: FlashlightRenderer) : GeoRenderLayer<FlashlightItem>(renderer) {
    val texture = rl("textures/item/flashlight.png")
    val glowTexture = rl("textures/item/flashlight_glowmask.png")

    override fun render(
        poseStack: PoseStack,
        animatable: FlashlightItem,
        bakedModel: BakedGeoModel,
        renderType: RenderType?,
        bufferSource: MultiBufferSource?,
        buffer: VertexConsumer?,
        partialTick: Float,
        packedLight: Int,
        packedOverlay: Int
    ) {
        // Transparent
        run {
            val type = RenderType.entityTranslucentEmissive(texture)
            val consumer = bufferSource?.getBuffer(type) ?: return@run
            renderer.reRender(
                bakedModel, poseStack, bufferSource, animatable, type,
                consumer, partialTick, LightTexture.FULL_SKY, packedOverlay,
                getRenderer().getRenderColor(animatable, partialTick, packedLight).argbInt()
            )
        }

        // Glow
        run {
            val type = RenderType.entityTranslucentEmissive(glowTexture)
            val glowConsumer = bufferSource?.getBuffer(type) ?: return@run

            val player = Minecraft.getInstance()?.player ?: return@run
            val baseColor = renderer.getRenderColor(animatable, partialTick, packedLight)
            val glow = (player.entityData.get(ModSynchedData.flashlightBattery) * 1.1f).coerceIn(0f..1f)

            val colour = Color.ofRGBA(
                (baseColor.red * glow).toInt(),
                (baseColor.green * glow).toInt(),
                (baseColor.blue * glow).toInt(),
                baseColor.alpha
            )

            renderer.reRender(
                bakedModel, poseStack, bufferSource, animatable, type,
                glowConsumer, partialTick, LightTexture.FULL_BRIGHT, packedOverlay,
                colour.argbInt
            )
        }
    }
}