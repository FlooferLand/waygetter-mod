package com.flooferland.waygetter.items

import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.ItemStack
import com.flooferland.waygetter.components.TattleStateDataComponent
import com.flooferland.waygetter.entities.TattletailEntity
import com.flooferland.waygetter.registry.ModComponents
import com.flooferland.waygetter.registry.ModItems
import com.flooferland.waygetter.registry.ModSounds
import com.flooferland.waygetter.systems.tattletail.TattleState
import com.flooferland.waygetter.utils.WaygetterRandom
import com.flooferland.waygetter.utils.WaygetterUtils
import software.bernie.geckolib.animatable.GeoAnimatable
import software.bernie.geckolib.animation.AnimatableManager
import software.bernie.geckolib.animation.AnimationController
import software.bernie.geckolib.animation.PlayState
import software.bernie.geckolib.animation.RawAnimation
import software.bernie.geckolib.constant.DataTickets

object TattletailClient {
    val barkMeTattletailAnim = RawAnimation.begin().thenPlay("animation.tattletail.me_tattletail")!!
    val barkThatsMeAnim = RawAnimation.begin().thenPlay("animation.tattletail.thats_me")!!
    val barkAhh = RawAnimation.begin().thenPlay("animation.tattletail.ahh")!!
    val barkItsDark = RawAnimation.begin().thenPlay("animation.tattletail.its_dark")!!
    val barkUhOh = RawAnimation.begin().thenPlay("animation.tattletail.uh_oh")!!
    val barkNightNight = RawAnimation.begin().thenPlay("animation.tattletail.night_night")!!
    val barkMeTired = RawAnimation.begin().thenPlay("animation.tattletail.me_tired")!!
    val barkBrushMe = RawAnimation.begin().thenPlay("animation.tattletail.brush_me")!!
    val barkGiveMeATreat = RawAnimation.begin().thenPlay("animation.tattletail.give_me_a_treat")!!

    /** Blockbench sound IDs to sounds */
    val sounds = mapOf(
        "tattletail_bark_me_tattletail" to ModSounds.TattleBarkMeTattletail,
        "tattletail_bark_thats_me" to ModSounds.TattleBarkThatsMe,
        "tattletail_bark_its_dark" to ModSounds.TattleBarkItsDark,
        "tattletail_bark_night_night" to ModSounds.TattleBarkNightNight,
        "tattletail_bark_me_tired" to ModSounds.TattleBarkMeTired,
        "tattletail_bark_uh_oh" to ModSounds.TattleBarkUhOh,
        "tattletail_bark_brush_me" to ModSounds.TattleBarkBrushMe,
        "tattletail_bark_give_me_a_treat" to ModSounds.TattleBarkGiveMeATreat
    )

    fun registerControllers(self: GeoAnimatable, controllers: AnimatableManager.ControllerRegistrar) {
        val controller = AnimationController(self, "main") { event ->
            val stack: ItemStack = when (self) {
                is TattletailItem -> event.getData(DataTickets.ITEMSTACK) ?: return@AnimationController PlayState.STOP
                is TattletailEntity -> self.itemStack
                else -> return@AnimationController PlayState.STOP
            }
            val state = stack.get(ModComponents.TattleStateData.type)?.state ?: return@AnimationController PlayState.CONTINUE

            if (event.controller.currentAnimation == null || event.controller.hasAnimationFinished()) {
                if (state.currentAnim.isNotEmpty()) event.controller.animationSpeed = 1.0
                when (state.currentAnim) {
                    "brush_me" -> event.controller.setAnimation(barkBrushMe)
                    "give_me_a_treat" -> event.controller.setAnimation(barkGiveMeATreat)
                    "its_dark" -> event.controller.setAnimation(barkItsDark)
                    "me_tattletail" -> event.controller.setAnimation(barkMeTattletailAnim)
                    "thats_me" -> event.controller.setAnimation(barkThatsMeAnim)
                    "tired" -> event.controller.setAnimation(arrayOf(barkNightNight, barkMeTired).random())
                    "uh_oh" -> {
                        event.controller.animationSpeed = 0.8 + WaygetterRandom.nextDouble() * 0.4
                        event.controller.setAnimation(barkUhOh)
                    }
                    "ahh" -> {
                        Minecraft.getInstance()?.let { mc ->
                            val level = mc.level ?: return@let
                            if (self is TattletailEntity) {
                                mc.soundManager.play(SimpleSoundInstance(ModSounds.TattleBarkAhh.event, SoundSource.NEUTRAL, 1.0f, 1.0f, level.random, self.blockPosition()))
                            } else {
                                mc.soundManager.play(SimpleSoundInstance.forUI(ModSounds.TattleBarkAhh.event, 1.0f, 1.0f))
                            }
                        }
                        event.controller.setAnimation(barkAhh)
                    }
                }
                state.currentAnim = ""
                event.controller.forceAnimationReset()

                // NOTE: CLIENT SIDE ONLY STATE
                if (self is TattletailItem) {
                    val stack = event.getData(DataTickets.ITEMSTACK)
                    stack?.set(ModComponents.TattleStateData.type, TattleStateDataComponent(state))
                }
            }
            PlayState.CONTINUE
        }
        controller.setSoundKeyframeHandler { event ->
            val mc = Minecraft.getInstance() ?: return@setSoundKeyframeHandler
            val level = mc.level ?: return@setSoundKeyframeHandler
            val sound = sounds[event.keyframeData.sound] ?: return@setSoundKeyframeHandler

            var pitch = 1.0f + (WaygetterRandom.nextFloat() - 0.5f) * 0.08f
            val volume = 1.0f + (WaygetterRandom.nextFloat() - 0.5f) * 0.1f
            when (sound) {
                ModSounds.TattleBarkUhOh -> {
                    pitch *= 0.8f + WaygetterRandom.nextFloat() * 0.4f
                }
                else -> {}
            }

            if (self is TattletailEntity) {
                mc.soundManager.play(SimpleSoundInstance(sound.event, SoundSource.NEUTRAL, volume, pitch, level.random, self.blockPosition()))
            } else {
                mc.soundManager.play(SimpleSoundInstance.forUI(sound.event, pitch, volume))
            }
        }
        controllers.add(controller)
    }
}