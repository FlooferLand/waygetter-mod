package com.flooferland.waygetter.entities

import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.*
import net.minecraft.network.chat.Component
import net.minecraft.network.syncher.*
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.*
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.*
import com.flooferland.waygetter.items.TattletailItem.Companion.REGISTER_CONTROLLERS
import com.flooferland.waygetter.registry.ModEntities
import com.flooferland.waygetter.registry.ModItems
import com.flooferland.waygetter.systems.tattletail.ITattleInstance
import com.flooferland.waygetter.systems.tattletail.TattleManager
import com.flooferland.waygetter.utils.Extensions.getCompoundOrNull
import software.bernie.geckolib.animatable.GeoEntity
import software.bernie.geckolib.animation.AnimatableManager
import software.bernie.geckolib.util.GeckoLibUtil
import kotlin.jvm.optionals.getOrNull

class TattletailEntity(level: Level) : Entity(ModEntities.Tattletail.type, level), GeoEntity, ITattleInstance {
    val cache = GeckoLibUtil.createInstanceCache(this)!!
    override fun getAnimatableInstanceCache() = cache
    override fun registerControllers(controllers: AnimatableManager.ControllerRegistrar) {
        REGISTER_CONTROLLERS(this, controllers)
    }

    var itemStack = ModItems.Tattletail.item.defaultInstance!!

    override fun isPickable() = true
    override fun canBeCollidedWith() = true

    override fun defineSynchedData(builder: SynchedEntityData.Builder) = Unit
    override fun readAdditionalSaveData(tag: CompoundTag) {
        tag.getCompoundOrNull("Item")?.let {
            itemStack = ItemStack.parse(level().registryAccess(), it).getOrNull() ?: return@let
        }
    }
    override fun addAdditionalSaveData(tag: CompoundTag) {
        tag.put("Item", itemStack.save(level().registryAccess()))
    }

    override fun getDisplayName() = itemStack.get(DataComponents.CUSTOM_NAME) ?: Component.empty()!!
    override fun getCustomName() = getDisplayName()

    //region ITattleInstance
    override val manager = TattleManager(this)
    override fun getTattleStack() = itemStack
    override fun getLevel() = level()!!
    override fun getPos() = blockPosition()!!
    override fun tick() {
        super.tick()
        if (!level().isClientSide) {
            manager.tick()
        }
    }
    // endregion

    override fun interact(player: Player, hand: InteractionHand): InteractionResult {
        if (!player.getItemInHand(hand).isEmpty) {
            return InteractionResult.SUCCESS
        }

        // Pickup
        if (!player.isCrouching) return InteractionResult.SUCCESS
        player.setItemInHand(hand, itemStack)
        remove(RemovalReason.DISCARDED)
        return InteractionResult.SUCCESS
    }
}