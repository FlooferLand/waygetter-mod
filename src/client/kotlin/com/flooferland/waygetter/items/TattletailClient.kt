package com.flooferland.waygetter.items

import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import com.flooferland.waygetter.components.TattleStateDataComponent
import com.flooferland.waygetter.entities.TattletailEntity
import com.flooferland.waygetter.registry.ModComponents
import com.flooferland.waygetter.registry.ModItems
import com.flooferland.waygetter.registry.ModSounds
import com.flooferland.waygetter.systems.tattletail.TattleState
import software.bernie.geckolib.animatable.GeoAnimatable
import software.bernie.geckolib.animation.AnimatableManager
import software.bernie.geckolib.animation.AnimationController
import software.bernie.geckolib.animation.PlayState
import software.bernie.geckolib.animation.RawAnimation
import software.bernie.geckolib.constant.DataTickets

object TattletailClient {
    val barkThatsMeAnim = RawAnimation.begin().thenPlay("animation.tattletail.thats_me")!!
    val barkMeTattletailAnim = RawAnimation.begin().thenPlay("animation.tattletail.me_tattletail")!!

    val sounds = mapOf(
        "tattletail_bark_thats_me" to ModSounds.TattleBarkThatsMe,
        "tattletail_bark_me_tattletail" to ModSounds.TattleBarkMeTattletail
    )

    fun registerControllers(self: GeoAnimatable, controllers: AnimatableManager.ControllerRegistrar) {
        val controller = AnimationController(self, "main") { event ->
            val state: TattleState = when (self) {
                is TattletailItem -> {
                    val stack = event.getData(DataTickets.ITEMSTACK) ?: return@AnimationController PlayState.CONTINUE
                    stack.get(ModComponents.TattleStateData.type)?.state ?: return@AnimationController PlayState.CONTINUE
                }
                is TattletailEntity -> {
                    self.state
                }
                else -> return@AnimationController PlayState.STOP
            }

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

                if (self is TattletailItem) {
                    val stack = event.getData(DataTickets.ITEMSTACK)
                    stack?.set(ModComponents.TattleStateData.type, TattleStateDataComponent(state))
                }
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