package com.flooferland.waygetter.items

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.*
import net.minecraft.world.item.context.UseOnContext
import com.flooferland.waygetter.components.TattleNeedsDataComponent
import com.flooferland.waygetter.entities.TattletailEntity
import com.flooferland.waygetter.registry.ModComponents
import java.util.function.Consumer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import org.apache.commons.lang3.mutable.MutableObject
import software.bernie.geckolib.animatable.GeoAnimatable
import software.bernie.geckolib.animatable.GeoItem
import software.bernie.geckolib.animatable.SingletonGeoAnimatable
import software.bernie.geckolib.animatable.client.GeoRenderProvider
import software.bernie.geckolib.animation.AnimatableManager
import software.bernie.geckolib.util.GeckoLibUtil

class TattletailItem(properties: Properties) : Item(properties), GeoItem {
    val renderProviderHolder = MutableObject<GeoRenderProvider>()
    val cache = GeckoLibUtil.createInstanceCache(this)!!
    override fun createGeoRenderer(consumer: Consumer<GeoRenderProvider>) = consumer.accept(renderProviderHolder.value)
    override fun getAnimatableInstanceCache() = cache

    init { SingletonGeoAnimatable.registerSyncedAnimatable(this) }

    override fun registerControllers(controllers: AnimatableManager.ControllerRegistrar) {
        REGISTER_CONTROLLERS(this, controllers)
    }

    override fun useOn(context: UseOnContext): InteractionResult {
        val stack = context.itemInHand
        val pos = context.clickLocation
        val level = context.level as? ServerLevel ?: return InteractionResult.PASS
        val player = context.player ?: return InteractionResult.PASS

        val entity = TattletailEntity(level)
        entity.setPos(pos)
        entity.yRot = 180f + context.rotation
        entity.itemStack = stack
        level.addFreshEntity(entity)

        player.setItemInHand(context.hand, ItemStack.EMPTY)
        return InteractionResult.SUCCESS
    }

    override fun allowComponentsUpdateAnimation(player: Player, hand: InteractionHand, oldStack: ItemStack, newStack: ItemStack): Boolean {
        return false
    }

    companion object {
        // I hate split source sets so much
        var REGISTER_CONTROLLERS: (self: GeoAnimatable, controllers: AnimatableManager.ControllerRegistrar) -> Unit = { self, controllers -> }

        init {
            ServerTickEvents.START_WORLD_TICK.register { level ->
                for (player in level.players()) {
                    if (level.gameTime % 10 != 0L) continue
                    val (hand, stack) = when {
                        player.mainHandItem.item is TattletailItem -> Pair(InteractionHand.MAIN_HAND, player.mainHandItem)
                        player.offhandItem.item is TattletailItem -> Pair(InteractionHand.OFF_HAND, player.offhandItem)
                        else -> continue
                    }
                    val needsComp = stack.components.getOrDefault(ModComponents.TattleNeedsData.type, TattleNeedsDataComponent())

                    val newStack = stack.copy()
                    val newNeeds = TattleNeedsDataComponent().apply {
                        needs.feed = (needsComp.needs.feed - 0.007f).coerceIn(0f..1f)
                        needs.groom = (needsComp.needs.groom - 0.005f).coerceIn(0f..1f)
                        needs.battery = (needsComp.needs.battery - 0.003f).coerceIn(0f..1f)
                    }
                    newStack.set(ModComponents.TattleNeedsData.type, newNeeds)
                    player.setItemInHand(hand, newStack)
                }
            }
        }
    }
}