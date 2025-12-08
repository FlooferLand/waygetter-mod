package com.flooferland.waygetter.models

import com.flooferland.waygetter.utils.rl
import software.bernie.geckolib.animatable.GeoAnimatable
import software.bernie.geckolib.model.GeoModel

class FlashlightModel<T: GeoAnimatable> : GeoModel<T>() {
    @Deprecated("Supressed")
    override fun getModelResource(animatable: T) = rl("geo/flashlight.geo.json")

    @Deprecated("Supressed")
    override fun getTextureResource(animatable: T) = rl("textures/item/flashlight.png")

    override fun getAnimationResource(animatable: T) = rl("animations/flashlight.animation.json")
}