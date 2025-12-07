package com.flooferland.waygetter.models

import net.minecraft.client.Minecraft
import net.minecraft.util.Mth
import com.flooferland.waygetter.entities.MamaEntity
import com.flooferland.waygetter.entities.TattletailEntity
import com.flooferland.waygetter.utils.rl
import software.bernie.geckolib.animatable.GeoAnimatable
import software.bernie.geckolib.animation.AnimationState
import software.bernie.geckolib.model.GeoModel
import kotlin.math.atan2

class MamaModel<T: GeoAnimatable> : GeoModel<T>() {
    @Deprecated("Supressed")
    override fun getModelResource(animatable: T) = rl("geo/mama.geo.json")

    @Deprecated("Supressed")
    override fun getTextureResource(animatable: T) = rl("textures/mama.png")

    override fun getAnimationResource(animatable: T) = rl("animations/mama.animation.json")

    override fun setCustomAnimations(animatable: T, instanceId: Long, state: AnimationState<T>) {
        // Body point
        val root = animationProcessor.getBone("root") ?: return
        if (animatable is TattletailEntity) {
            root.rotY = (animatable.yRot * -1f) * Mth.DEG_TO_RAD
        } else {
            root.rotY = 0f
        }

        // Head
        run {
            val top = animationProcessor.getBone("top") ?: return@run
            top.rotY = 0f

            val player = Minecraft.getInstance()?.player ?: return@run
            if (animatable is MamaEntity) {
                top.rotY = atan2(player.x - animatable.x, player.z - animatable.z).toFloat()
            }
        }
    }
}