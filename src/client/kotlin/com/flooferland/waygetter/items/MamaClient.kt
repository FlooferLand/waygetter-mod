package com.flooferland.waygetter.items

import com.flooferland.waygetter.entities.MamaEntity
import software.bernie.geckolib.animatable.GeoAnimatable
import software.bernie.geckolib.animation.AnimatableManager
import software.bernie.geckolib.animation.AnimationController
import software.bernie.geckolib.animation.PlayState
import software.bernie.geckolib.animation.RawAnimation

object MamaClient {
    val walkAnim = RawAnimation.begin().thenPlay("animation.mama.walk")!!

    fun registerControllers(self: GeoAnimatable, controllers: AnimatableManager.ControllerRegistrar) {
        val controller = AnimationController(self, "main") { event ->
            val entity = self as? MamaEntity ?: return@AnimationController PlayState.CONTINUE
            if (entity.moveDist > 0f) {
                event.controller.setAnimation(walkAnim)
                return@AnimationController PlayState.CONTINUE
            }

            // Resetting
            event.controller.setAnimation(null)
            PlayState.CONTINUE
        }
        controllers.add(controller)
    }
}