package com.flooferland.waygetter.models

import com.flooferland.waygetter.utils.rl
import software.bernie.geckolib.animatable.GeoAnimatable
import software.bernie.geckolib.model.GeoModel

class TattletailModel<T: GeoAnimatable> : GeoModel<T>() {
    @Deprecated("Supressed")
    override fun getModelResource(animatable: T?) = rl("geo/tattletail.geo.json")

    @Deprecated("Supressed")
    override fun getTextureResource(animatable: T?) = rl("textures/tattletail.png")

    override fun getAnimationResource(animatable: T?) = rl("animations/tattletail.animation.json")
}