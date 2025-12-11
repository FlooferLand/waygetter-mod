package com.flooferland.waygetter.utils

import net.minecraft.core.registries.*
import net.minecraft.nbt.*
import net.minecraft.network.chat.*
import net.minecraft.resources.*
import net.minecraft.server.level.*
import net.minecraft.world.entity.*
import net.minecraft.world.entity.player.*
import net.minecraft.world.item.*
import net.minecraft.world.level.block.entity.*
import com.flooferland.waygetter.items.FlashlightItem
import com.flooferland.waygetter.items.TattletailItem
import java.util.UUID

@Suppress("unused")
object Extensions {
    // Waygetter mod specific
    fun Player.isProvokingMama(): Boolean {
        return isHolding { it.item is TattletailItem } || inventory.hasAnyMatching { it.item is TattletailItem }
    }
    fun Player.canMakeSound(): Boolean {
        return isProvokingMama() || isHolding { it.item is FlashlightItem }
    }

    // Resource locations
    fun ResourceLocation.blockPath(): ResourceLocation {
        return this.withPrefix("block/");
    }
    fun ResourceLocation.itemPath(): ResourceLocation {
        return this.withPrefix("item/");
    }

    /** Calls setChanged and sendBlockUpdated */
    fun BlockEntity.markDirtyNotifyAll() {
        setChanged()
        level?.sendBlockUpdated(this.blockPos, blockState, blockState, 0)
    }

    @DslMarker annotation class BlockEntityApplyDsl;
    @BlockEntityApplyDsl
    fun <T: BlockEntity> T.applyChange(rerender: Boolean, change: T.() -> Unit) {
        change(this)
        markDirtyNotifyAll()
    }

    fun Entity.hasTag(name: String): Boolean =
        tags.contains(name)

    fun ServerLevel.getRandomPlayer(predicate: (ServerPlayer) -> Boolean): ServerPlayer? {
        val picked = this.getPlayers(predicate).randomOrNull() ?: return null
        return picked
    }

    /** Gets the `ResourceLocation` of an item. Very annoying Mojang could not add this, bruh. */
    fun Item.getResourceLocation(): ResourceLocation {
        return BuiltInRegistries.ITEM.getKey(this)
    }

    /** Missing from Minecraft's standard library */
    fun LivingEntity.getHeldItem(filter: (ItemStack) -> Boolean): ItemStack? =
        when {
            filter(mainHandItem) -> mainHandItem
            filter(offhandItem) -> offhandItem
            else -> null
        }

    //region Component
    fun MutableComponent.hover(text: String) = hover(Component.literal(text))
    fun MutableComponent.hover(comp: Component) =
        withStyle { it.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, comp)) }!!
    fun MutableComponent.click(action: ClickEvent.Action, value: String) =
        withStyle { it.withClickEvent(ClickEvent(action, value)) }!!
    //endregion

    /** Converts seconds to ticks */
    fun Int.secsToTicks(): Int = this * 20

    //region Compound get functions, since these change for 1.21.5+
    fun CompoundTag.getBooleanOrNull(string: String): Boolean? =
        if (contains(string)) getBoolean(string) else null
    fun CompoundTag.getByteOrNull(string: String): Byte? =
        if (contains(string)) getByte(string) else null
    fun CompoundTag.getIntOrNull(string: String): Int? =
        if (contains(string)) getInt(string) else null
    fun CompoundTag.getFloatOrNull(string: String): Float? =
        if (contains(string)) getFloat(string) else null
    fun CompoundTag.getDoubleOrNull(string: String): Double? =
        if (contains(string)) getDouble(string) else null
    fun CompoundTag.getStringOrNull(string: String): String? =
        if (contains(string)) getString(string) else null
    fun CompoundTag.getIntArrayOrNull(string: String): IntArray? =
        if (contains(string)) getIntArray(string) else null
    fun CompoundTag.getByteArrayOrNull(string: String): ByteArray? =
        if (contains(string)) getByteArray(string) else null
    fun CompoundTag.getUuidOrNull(key: String): UUID? =
        contains(key).let { if (it) getUUID(key) else null }
    fun CompoundTag.getCompoundOrNull(key: String): CompoundTag? =
        contains(key).let { if (it) getCompound(key) else null }
    //endregion
}