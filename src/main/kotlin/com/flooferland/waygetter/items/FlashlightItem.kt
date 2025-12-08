package com.flooferland.waygetter.items

import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.ServerLevelAccessor
import com.flooferland.waygetter.systems.NoiseTracker
import java.util.function.Consumer
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

    override fun onUseTick(level: Level, livingEntity: LivingEntity, stack: ItemStack, remainingUseDuration: Int) {
        val level = level as? ServerLevel ?: return
        val player = livingEntity as? ServerPlayer ?: return
        NoiseTracker.add(player, 0.3f)
    }

    companion object {
        var REGISTER_CONTROLLERS: (self: FlashlightItem, controllers: AnimatableManager.ControllerRegistrar) -> Unit = { self, controllers -> }
    }
}