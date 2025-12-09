package com.flooferland.waygetter.renderers

import net.minecraft.client.*
import net.minecraft.client.renderer.*
import net.minecraft.client.renderer.entity.*
import com.flooferland.waygetter.entities.MamaEntity
import com.flooferland.waygetter.models.MamaModel
import com.flooferland.waygetter.utils.Extensions.isProvokingMama
import software.bernie.geckolib.cache.texture.AutoGlowingTexture
import software.bernie.geckolib.renderer.GeoEntityRenderer
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer

class MamaEntityRenderer(context: EntityRendererProvider.Context) : GeoEntityRenderer<MamaEntity>(context, MamaModel()) {
    init {
        addRenderLayer(AutoGlowingGeoLayer(this))
    }
}