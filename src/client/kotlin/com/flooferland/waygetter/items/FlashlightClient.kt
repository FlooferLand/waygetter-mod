package com.flooferland.waygetter.items

import net.minecraft.client.*
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import com.flooferland.waygetter.registry.ModSounds
import com.flooferland.waygetter.utils.WaygetterUtils
import software.bernie.geckolib.animation.AnimatableManager
import software.bernie.geckolib.animation.AnimationController
import software.bernie.geckolib.animation.PlayState
import software.bernie.geckolib.animation.RawAnimation
import software.bernie.geckolib.constant.DataTickets

// TODO: Replicate over when a player's flashlight is recharging so other players see/hear it

object FlashlightClient {
    val rechargeAnim = RawAnimation.begin().thenPlay("animation.flashlight.recharge")!!

    fun registerControllers(self: FlashlightItem, controllers: AnimatableManager.ControllerRegistrar) {
        val controller = AnimationController(self, "main") { event ->
            val player = Minecraft.getInstance().player ?: return@AnimationController PlayState.CONTINUE
            val stack = event.getData(DataTickets.ITEMSTACK) ?: return@AnimationController PlayState.CONTINUE

            if (player.useItem.item == self && !player.useItem.isEmpty) {
                event.controller.setAnimation(rechargeAnim)
            } else {
                event.controller.setAnimation(null)
                return@AnimationController PlayState.STOP
            }

            PlayState.CONTINUE
        }
        controller.setSoundKeyframeHandler { event ->
            val mc = Minecraft.getInstance() ?: return@setSoundKeyframeHandler
            val sound = ModSounds.FlashlightShake
            val pitch = 1.0f + (WaygetterUtils.random.nextFloat() - 0.5f) * 0.08f
            val volume = 1.0f + (WaygetterUtils.random.nextFloat() - 0.5f) * 0.1f
            mc.soundManager.play(SimpleSoundInstance.forUI(sound.event, pitch, volume))
        }
        controllers.add(controller)
    }
}