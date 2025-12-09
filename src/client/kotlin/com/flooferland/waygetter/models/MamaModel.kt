package com.flooferland.waygetter.models

import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import com.flooferland.waygetter.entities.MamaEntity
import com.flooferland.waygetter.utils.Extensions.isProvokingMama
import com.flooferland.waygetter.utils.rl
import software.bernie.geckolib.animatable.GeoAnimatable
import software.bernie.geckolib.animation.AnimationState
import software.bernie.geckolib.model.GeoModel
import kotlin.math.atan2

// TODO: Add smooth transitions between all the states using linear interp

class MamaModel<T: GeoAnimatable> : GeoModel<T>() {
    val defaultTexture = rl("textures/mama.png")
    val spookyTexture = rl("textures/mama_spooky.png")

    @Deprecated("Supressed")
    override fun getModelResource(animatable: T) = rl("geo/mama.geo.json")

    @Deprecated("Supressed")
    override fun getTextureResource(animatable: T): ResourceLocation {
        val player = Minecraft.getInstance()?.player ?: return defaultTexture
        if (player.isProvokingMama()) {
            return spookyTexture
        }
        return defaultTexture
    }

    override fun getAnimationResource(animatable: T) = rl("animations/mama.animation.json")

    override fun setCustomAnimations(animatable: T, instanceId: Long, state: AnimationState<T>) {
        if (animatable !is MamaEntity) return
        val player = Minecraft.getInstance()?.player ?: return
        val root = animationProcessor.getBone("root") ?: return
        val top = animationProcessor.getBone("top") ?: return
        val jaw = animationProcessor.getBone("jaw") ?: return
        root.rotY = 0f
        top.rotY = 0f
        jaw.posY = 0f

        val provoked = player.isProvokingMama()

        // Jaw spooky
        jaw.posY = if (provoked) -0.7f else 0f

        // Head - if it works, it works
        if (provoked) {
            val playerAngle = atan2(player.z - animatable.z, player.x - animatable.x).toFloat()
            val yaw = (animatable.yHeadRot * Mth.DEG_TO_RAD) + (Mth.PI / 2)
            top.rotY = yaw - playerAngle
        }
    }
}