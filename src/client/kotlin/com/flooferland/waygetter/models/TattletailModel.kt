package com.flooferland.waygetter.models

import net.minecraft.client.Minecraft
import net.minecraft.util.Mth
import com.flooferland.waygetter.entities.TattletailEntity
import com.flooferland.waygetter.items.TattletailItem
import com.flooferland.waygetter.registry.ModComponents
import com.flooferland.waygetter.systems.tattletail.ITattleInstance
import com.flooferland.waygetter.systems.tattletail.TattleItemStackTempInstance
import com.flooferland.waygetter.utils.lerp
import com.flooferland.waygetter.utils.rl
import software.bernie.geckolib.animatable.GeoAnimatable
import software.bernie.geckolib.animation.AnimationState
import software.bernie.geckolib.constant.DataTickets
import software.bernie.geckolib.constant.dataticket.DataTicket
import software.bernie.geckolib.model.GeoModel

class TattletailModel<T: GeoAnimatable> : GeoModel<T>() {
    val smooths = HashMap<String, Float>()

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

        // Getting things
        val eyes = animationProcessor.getBone("eyes") ?: return
        val stack = when (animatable) {
            is TattletailEntity -> animatable.itemStack
            is TattletailItem -> state.getData(DataTickets.ITEMSTACK)
            else -> null
        } ?: return
        val state = stack.get(ModComponents.TattleStateData.type)?.state ?: return
        val delta = Minecraft.getInstance().timer.gameTimeDeltaTicks

        // Eyes
        run {
            val maxMove = 0.4f
            val eyesTarget = (maxMove * state.lookDir.toFloat()).coerceIn(-maxMove..maxMove)
            val current = smooths.getOrPut("eyes") { eyesTarget }
            val smooth = lerp(current, eyesTarget, 0.1f * delta)
            eyes.posX = smooth
            smooths["eyes"] = smooth
        }
    }
}