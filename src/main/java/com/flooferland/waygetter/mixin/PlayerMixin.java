package com.flooferland.waygetter.mixin;

import com.flooferland.waygetter.registry.ModSynchedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin {
	@Inject(method = "defineSynchedData", at = @At("TAIL"))
	private void waygetter_defineSynchedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
		builder.define(ModSynchedData.INSTANCE.getFlashlightBattery(), 0f);
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void waygetter_addAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
		var self = (Player) (Object) this;
		var flashlight = self.getEntityData().get(ModSynchedData.INSTANCE.getFlashlightBattery());
		tag.putFloat("Waygetter-Flashlight", flashlight);
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void waygetter_readAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
		var self = (Player) (Object) this;
		self.getEntityData().set(ModSynchedData.INSTANCE.getFlashlightBattery(), tag.getFloat("Waygetter-Flashlight"));
	}
}
