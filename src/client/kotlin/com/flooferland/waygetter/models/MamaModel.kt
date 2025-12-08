package com.flooferland.waygetter.models

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import com.flooferland.waygetter.entities.MamaEntity
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
        val player = Minecraft.getInstance()?.player ?: return
        val root = animationProcessor.getBone("root") ?: return
        val top = animationProcessor.getBone("top") ?: return
        root.rotY = 0f
        top.rotY = 0f

        // Body point
        if (animatable !is MamaEntity) {
            return
        }

        // Head - if it works, it works
        run {
            val playerAngle = atan2(player.z - animatable.z, player.x - animatable.x).toFloat()
            val yaw = (animatable.yHeadRot * Mth.DEG_TO_RAD) + (Mth.PI / 2)
            top.rotY = yaw - playerAngle
        }
    }
}