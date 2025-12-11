package com.flooferland.waygetter.items

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import com.flooferland.waygetter.registry.ModItems
import com.flooferland.waygetter.registry.ModSynchedData
import com.flooferland.waygetter.systems.NoiseTracker
import com.flooferland.waygetter.utils.Extensions.getHeldItem
import com.flooferland.waygetter.utils.Extensions.isProvokingMama
import java.util.function.Consumer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import org.apache.commons.lang3.mutable.MutableObject
import software.bernie.geckolib.animatable.GeoItem
import software.bernie.geckolib.animatable.SingletonGeoAnimatable
import software.bernie.geckolib.animatable.client.GeoRenderProvider
import software.bernie.geckolib.animation.AnimatableManager
import software.bernie.geckolib.util.GeckoLibUtil

class FlashlightItem(properties: Properties) : Item(properties), GeoItem {
    val renderProviderHolder = MutableObject<GeoRenderProvider>()
    val cache = GeckoLibUtil.createInstanceCache(this)!!
    override fun createGeoRenderer(consumer: Consumer<GeoRenderProvider>) = consumer.accept(renderProviderHolder.value)
    override fun getAnimatableInstanceCache() = cache

    init { SingletonGeoAnimatable.registerSyncedAnimatable(this) }

    override fun registerControllers(controllers: AnimatableManager.ControllerRegistrar) {
        FlashlightItem.REGISTER_CONTROLLERS(this, controllers)
    }

    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(usedHand)
        player.startUsingItem(usedHand)
        return InteractionResultHolder.success(stack)
    }

    override fun finishUsingItem(stack: ItemStack, level: Level, livingEntity: LivingEntity): ItemStack? {
        return super.finishUsingItem(stack, level, livingEntity)
    }

    override fun inventoryTick(stack: ItemStack, level: Level, entity: Entity, slotId: Int, isSelected: Boolean) {
        if (level !is ServerLevel) return
        if (entity !is ServerPlayer) return
        if (!isSelected) return
        val newBattery = (entity.entityData.get(ModSynchedData.flashlightBattery) - DECAY).coerceIn(0f..1f)
        entity.entityData.set(ModSynchedData.flashlightBattery, newBattery)
    }

    override fun onUseTick(level: Level, livingEntity: LivingEntity, stack: ItemStack, remainingUseDuration: Int) {
        if (level !is ServerLevel) return
        val player = livingEntity as? ServerPlayer ?: return
        player.entityData.set(ModSynchedData.flashlightBattery, (player.entityData.get(ModSynchedData.flashlightBattery) + GAIN).coerceIn(0f..1f))
        NoiseTracker.add(player, NoiseTracker.NOISE_MEDIUM)
    }

    companion object {
        const val GAIN = 0.035f
        const val DECAY = 0.005f

        var REGISTER_CONTROLLERS: (self: FlashlightItem, controllers: AnimatableManager.ControllerRegistrar) -> Unit = { self, controllers -> }
    }
}