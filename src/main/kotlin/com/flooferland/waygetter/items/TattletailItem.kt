package com.flooferland.waygetter.items

import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponents
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.*
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import com.flooferland.waygetter.blocks.BrushBlock
import com.flooferland.waygetter.blocks.ChargerBlock
import com.flooferland.waygetter.components.TattleNeedsDataComponent
import com.flooferland.waygetter.components.TattleStateDataComponent
import com.flooferland.waygetter.entities.TattletailEntity
import com.flooferland.waygetter.registry.ModBlocks
import com.flooferland.waygetter.registry.ModComponents
import com.flooferland.waygetter.systems.tattletail.TattleItemStackInstance
import com.flooferland.waygetter.utils.Extensions.getResourceLocation
import com.flooferland.waygetter.utils.Extensions.secsToTicks
import com.flooferland.waygetter.utils.playerMadeSound
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

    data class NeedUse(val type: NeedUseType, val useStack: ItemStack)
    enum class NeedUseType { Charge, Feed, Groom }
    fun getOtherItemUse(player: ServerPlayer, blockPos: BlockPos? = null): NeedUse? {
        val level = player.serverLevel() ?: return null
        val mainHandItem = player.getItemInHand(InteractionHand.MAIN_HAND)
        val offHandItem = player.getItemInHand(InteractionHand.OFF_HAND)
        val otherStack = when {
            mainHandItem.item !is TattletailItem -> mainHandItem
            offHandItem.item !is TattletailItem -> offHandItem
            else -> return null
        } ?: ItemStack.EMPTY

        val type = when {
            blockPos != null -> {
                val blockState = level.getBlockState(blockPos)
                when (blockState.block) {
                    is ChargerBlock -> NeedUseType.Charge
                    is BrushBlock -> NeedUseType.Groom
                    else -> null
                }
            }
            otherStack.has(DataComponents.FOOD) -> NeedUseType.Feed
            otherStack.`is` { it.value().getResourceLocation() == ModBlocks.Brush.id } -> NeedUseType.Groom
            else -> null
        } ?: return null
        return NeedUse(type, otherStack)
    }

    // Block use
    override fun useOn(context: UseOnContext): InteractionResult {
        val stack = context.itemInHand
        val pos = context.clickLocation
        val level = context.level as? ServerLevel ?: return InteractionResult.PASS
        val player = context.player as? ServerPlayer ?: return InteractionResult.PASS
        if (player.useItem == stack.item) return InteractionResult.PASS

        // Charging
        val use = getOtherItemUse(player, context.clickedPos)
        if (use != null) {
            val needsComp = stack.get(ModComponents.TattleNeedsData.type) ?: return InteractionResult.PASS
            when (use.type) {
                NeedUseType.Charge -> {
                    needsComp.needs.battery += 0.05f
                }
                NeedUseType.Groom -> {
                    needsComp.needs.groom += 0.05f
                }
                else -> {}
            }
            stack.set(ModComponents.TattleNeedsData.type, needsComp)
            return InteractionResult.CONSUME
        }

        // Placing down the Tattletail
        val entity = TattletailEntity(level)
        entity.setPos(pos)
        entity.yRot = 180f + context.rotation
        entity.itemStack = stack
        level.addFreshEntity(entity)

        player.setItemInHand(context.hand, ItemStack.EMPTY)
        return InteractionResult.SUCCESS
    }

    override fun getUseAnimation(stack: ItemStack) = UseAnim.NONE
    override fun getUseDuration(stack: ItemStack, entity: LivingEntity) = 1.secsToTicks()
    override fun use(level: Level, player: Player, tattleHand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(tattleHand) ?: return InteractionResultHolder.pass(ItemStack.EMPTY)
        if (stack.item !is TattletailItem) return InteractionResultHolder.pass(stack)
        val level = level as? ServerLevel ?: return InteractionResultHolder.consume(stack)

        val otherHand = when (tattleHand) {
            InteractionHand.OFF_HAND -> InteractionHand.MAIN_HAND
            InteractionHand.MAIN_HAND -> InteractionHand.OFF_HAND
        }
        val otherStack = player.getItemInHand(otherHand)
        if (otherStack == null || otherStack.isEmpty) return InteractionResultHolder.pass(stack)

        when {
            otherStack.components.has(DataComponents.FOOD) -> {
                player.startUsingItem(tattleHand)
            }
        }
        return InteractionResultHolder.consume(stack)
    }
    override fun onUseTick(level: Level, entity: LivingEntity, stack: ItemStack, remainingUseDuration: Int) {
        val player = entity as? ServerPlayer ?: return
        val use = getOtherItemUse(player) ?: return

        val needsComp = stack.get(ModComponents.TattleNeedsData.type) ?: return
        when (use.type) {
            NeedUseType.Feed -> {
                if (level.gameTime % 5 == 0L) playerMadeSound(level, player, SoundEvents.GENERIC_EAT)
            }
            else -> {}
        }
        stack.set(ModComponents.TattleNeedsData.type, needsComp)
    }
    override fun releaseUsing(stack: ItemStack, level: Level, entity: LivingEntity, timeCharged: Int) {
        val player = entity as? ServerPlayer ?: return
        val use = getOtherItemUse(player) ?: return

        val needsComp = stack.get(ModComponents.TattleNeedsData.type) ?: return
        when (use.type) {
            NeedUseType.Feed -> {
                val foodComp = use.useStack.get(DataComponents.FOOD) ?: return
                val nutrition = foodComp.nutrition.toFloat() / getUseDuration(stack, entity).toFloat()
                needsComp.needs.feed += nutrition
                use.useStack.shrink(1)
            }
            else -> {}
        }
        stack.set(ModComponents.TattleNeedsData.type, needsComp)
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
                    // Updating Tattletail state
                    run {
                        var stack: ItemStack? = null
                        if (player.mainHandItem.item is TattletailItem)
                            stack = player.mainHandItem
                        if (player.offhandItem.item is TattletailItem)
                            stack = player.offhandItem
                        if (stack == null) return@run
                        val instance = TattleItemStackInstance(stack, player)
                        instance.manager.tick()
                    }

                    // Updating Tattletail needs
                    run {
                        if (level.gameTime % 10 != 0L) return@run
                        val (hand, stack) = when {
                            player.mainHandItem.item is TattletailItem -> Pair(InteractionHand.MAIN_HAND, player.mainHandItem)
                            player.offhandItem.item is TattletailItem -> Pair(InteractionHand.OFF_HAND, player.offhandItem)
                            else -> return@run
                        }
                        val needsComp = stack.getOrDefault(ModComponents.TattleNeedsData.type, TattleNeedsDataComponent())

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
}