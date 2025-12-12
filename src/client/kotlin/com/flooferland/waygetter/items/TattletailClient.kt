package com.flooferland.waygetter.items

import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundSource
import com.flooferland.waygetter.components.TattleStateDataComponent
import com.flooferland.waygetter.entities.TattletailEntity
import com.flooferland.waygetter.registry.ModComponents
import com.flooferland.waygetter.registry.ModItems
import com.flooferland.waygetter.registry.ModSounds
import com.flooferland.waygetter.systems.tattletail.TattleState
import com.flooferland.waygetter.utils.WaygetterUtils
import software.bernie.geckolib.animatable.GeoAnimatable
import software.bernie.geckolib.animation.AnimatableManager
import software.bernie.geckolib.animation.AnimationController
import software.bernie.geckolib.animation.PlayState
import software.bernie.geckolib.animation.RawAnimation
import software.bernie.geckolib.constant.DataTickets

object TattletailClient {
    val barkThatsMeAnim = RawAnimation.begin().thenPlay("animation.tattletail.thats_me")!!
    val barkMeTattletailAnim = RawAnimation.begin().thenPlay("animation.tattletail.me_tattletail")!!
    val barkItsDark = RawAnimation.begin().thenPlay("animation.tattletail.its_dark")!!
    val barkAhh = RawAnimation.begin().thenPlay("animation.tattletail.ahh")!!

    /** Blockbench sound IDs to sounds */
    val sounds = mapOf(
        "tattletail_bark_thats_me" to ModSounds.TattleBarkThatsMe,
        "tattletail_bark_me_tattletail" to ModSounds.TattleBarkMeTattletail,
        "tattletail_bark_its_dark" to ModSounds.TattleBarkItsDark
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

            if (event.controller.currentAnimation == null || event.controller.hasAnimationFinished()) {
                when (state.currentAnim) {
                    "thats_me" -> event.controller.setAnimation(barkThatsMeAnim)
                    "me_tattletail" -> event.controller.setAnimation(barkMeTattletailAnim)
                    "its_dark" -> event.controller.setAnimation(barkItsDark)
                    "ahh" -> {
                        Minecraft.getInstance()?.let { mc ->
                            if (self is TattletailEntity) {
                                mc.soundManager.play(SimpleSoundInstance(ModSounds.TattleBarkAhh.event, SoundSource.NEUTRAL, 1.0f, 1.0f, WaygetterUtils.random, self.blockPosition()))
                            } else {
                                mc.soundManager.play(SimpleSoundInstance.forUI(ModSounds.TattleBarkAhh.event, 1.0f, 1.0f))
                            }
                        }
                        event.controller.setAnimation(barkAhh)
                    }
                }
                state.currentAnim = ""
                event.controller.forceAnimationReset()
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
            val pitch = 1.0f + (WaygetterUtils.random.nextFloat() - 0.5f) * 0.08f
            val volume = 1.0f + (WaygetterUtils.random.nextFloat() - 0.5f) * 0.1f
            if (self is TattletailEntity) {
                mc.soundManager.play(SimpleSoundInstance(sound.event, SoundSource.NEUTRAL, volume, pitch, WaygetterUtils.random, self.blockPosition()))
            } else {
                mc.soundManager.play(SimpleSoundInstance.forUI(sound.event, pitch, volume))
            }
        }
        controllers.add(controller)
    }
}