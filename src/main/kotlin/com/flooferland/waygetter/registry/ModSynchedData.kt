package com.flooferland.waygetter.registry

import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.player.Player

/** Keys for storing synched data on the player (yes its mispelled on purpose, dunno why Mojang spells it that way) */
object ModSynchedData {
    val flashlightBattery = SynchedEntityData.defineId(Player::class.java, EntityDataSerializers.FLOAT)!!
}