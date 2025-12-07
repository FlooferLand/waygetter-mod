package com.flooferland.waygetter.items

import net.minecraft.server.level.*
import net.minecraft.world.*
import net.minecraft.world.item.*
import net.minecraft.world.item.context.*
import com.flooferland.waygetter.entities.MamaEntity
import com.flooferland.waygetter.entities.TattletailEntity
import java.util.function.Consumer
import org.apache.commons.lang3.mutable.MutableObject
import software.bernie.geckolib.animatable.GeoAnimatable
import software.bernie.geckolib.animatable.GeoItem
import software.bernie.geckolib.animatable.SingletonGeoAnimatable
import software.bernie.geckolib.animatable.client.GeoRenderProvider
import software.bernie.geckolib.animation.AnimatableManager
import software.bernie.geckolib.util.GeckoLibUtil

// TODO: Merge this with the Tattletail item
class MamaItem(properties: Properties) : Item(properties), GeoItem {
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

        val entity = MamaEntity(level)
        entity.setPos(pos)
        entity.yRot = 180f + context.rotation
        level.addFreshEntity(entity)

        player.setItemInHand(context.hand, ItemStack.EMPTY)
        return InteractionResult.SUCCESS
    }

    // I hate split source sets so much
    companion object {
        var REGISTER_CONTROLLERS: (self: GeoAnimatable, controllers: AnimatableManager.ControllerRegistrar) -> Unit = { self, controllers -> }
    }
}