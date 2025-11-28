package com.flooferland.waygetter.items

import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.sounds.SoundSource
import com.flooferland.waygetter.components.TattleStateDataComponent
import com.flooferland.waygetter.registry.ModComponents
import com.flooferland.waygetter.registry.ModItems
import com.flooferland.waygetter.registry.ModSounds
import software.bernie.geckolib.animation.AnimatableManager
import software.bernie.geckolib.animation.AnimationController
import software.bernie.geckolib.animation.PlayState
import software.bernie.geckolib.animation.RawAnimation
import software.bernie.geckolib.constant.DataTickets

object TattletailItemClient {
    val barkThatsMeAnim = RawAnimation.begin().thenPlay("animation.tattletail.thats_me")!!
    val barkMeTattletailAnim = RawAnimation.begin().thenPlay("animation.tattletail.me_tattletail")!!

    val sounds = mapOf(
        "tattletail_bark_thats_me" to ModSounds.TattleBarkThatsMe,
        "tattletail_bark_me_tattletail" to ModSounds.TattleBarkMeTattletail
    )

    fun registerControllers(controllers: AnimatableManager.ControllerRegistrar) {
        val self = ModItems.Tattletail.item as TattletailItem
        val controller = AnimationController(self, "main") { event ->
            val stack = event.getData(DataTickets.ITEMSTACK) ?: return@AnimationController PlayState.CONTINUE
            val state = stack.get(ModComponents.TattleStateData.type)?.state ?: return@AnimationController PlayState.CONTINUE

            if (state.currentAnim != state.lastAnim) {
                state.lastAnim = state.currentAnim
                println("Controller played \"${state.currentAnim}\"")
                when (state.currentAnim) {
                    "thats_me" -> {
                        event.controller.setAnimation(barkThatsMeAnim)
                    }

                    "me_tattletail" -> {
                        event.controller.setAnimation(barkMeTattletailAnim)
                    }
                }
                stack.set(ModComponents.TattleStateData.type, TattleStateDataComponent(state))
            }
            PlayState.CONTINUE
        }
        controller.setSoundKeyframeHandler { event ->
            val mc = Minecraft.getInstance() ?: return@setSoundKeyframeHandler
            val sound = sounds[event.keyframeData.sound] ?: return@setSoundKeyframeHandler
            mc.soundManager.play(SimpleSoundInstance.forUI(sound.event, 1.0f, 1.0f))
        }
        controllers.add(controller)
    }
}