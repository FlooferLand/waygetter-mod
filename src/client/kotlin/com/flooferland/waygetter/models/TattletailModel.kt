package com.flooferland.waygetter.models

import net.minecraft.util.Mth
import com.flooferland.waygetter.entities.TattletailEntity
import com.flooferland.waygetter.utils.rl
import software.bernie.geckolib.animatable.GeoAnimatable
import software.bernie.geckolib.animation.AnimationState
import software.bernie.geckolib.model.GeoModel

class TattletailModel<T: GeoAnimatable> : GeoModel<T>() {
    @Deprecated("Supressed")
    override fun getModelResource(animatable: T) = rl("geo/tattletail.geo.json")

    @Deprecated("Supressed")
    override fun getTextureResource(animatable: T) = rl("textures/tattletail.png")

    override fun getAnimationResource(animatable: T) = rl("animations/tattletail.animation.json")

    override fun setCustomAnimations(animatable: T, instanceId: Long, state: AnimationState<T>) {
        val root = animationProcessor.getBone("root") ?: return
        if (animatable is TattletailEntity) {
            root.rotY = (animatable.yRot * -1f) * Mth.DEG_TO_RAD
        } else {
            root.rotY = 0f
        }
    }
}