package com.flooferland.waygetter.items

import net.minecraft.world.item.*
import java.util.function.Consumer
import org.apache.commons.lang3.mutable.MutableObject
import software.bernie.geckolib.animatable.GeoItem
import software.bernie.geckolib.animatable.SingletonGeoAnimatable
import software.bernie.geckolib.animatable.client.GeoRenderProvider
import software.bernie.geckolib.animation.AnimatableManager
import software.bernie.geckolib.animation.RawAnimation
import software.bernie.geckolib.util.GeckoLibUtil

class TattletailItem(properties: Properties) : Item(properties), GeoItem {
    val renderProviderHolder = MutableObject<GeoRenderProvider>()
    val cache = GeckoLibUtil.createInstanceCache(this)!!
    override fun createGeoRenderer(consumer: Consumer<GeoRenderProvider>) = consumer.accept(renderProviderHolder.value)
    override fun getAnimatableInstanceCache() = cache

    init { SingletonGeoAnimatable.registerSyncedAnimatable(this) }

    override fun registerControllers(controllers: AnimatableManager.ControllerRegistrar) {
        REGISTER_CONTROLLERS(controllers)
    }

    // I hate split source sets so much
    companion object {
        var REGISTER_CONTROLLERS: (controllers: AnimatableManager.ControllerRegistrar) -> Unit = { }
    }
}