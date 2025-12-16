package com.flooferland.waygetter.mixin;

import com.flooferland.waygetter.entities.MamaEntity;
import com.flooferland.waygetter.registry.ModSynchedData;
import com.flooferland.waygetter.utils.Extensions;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

	@Inject(method = "startSleepInBed", at = @At("HEAD"), cancellable = true)
	private void waygetter_preventSleep(BlockPos bedPos, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir) {
		var self = (Player) (Object) this;
		if (Extensions.INSTANCE.isProvokingMama(self)) {
			cir.setReturnValue(Either.left(Player.BedSleepingProblem.NOT_SAFE));
		}
	}
}
