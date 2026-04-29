package com.bawnorton.neruina.mixin.errorable;

import com.bawnorton.neruina.extend.Errorable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

//? if >1.20.1 {
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.component.CustomData;
//?}

@Mixin(ItemStack.class)
abstract class ItemStackMixin implements Errorable {
	//? if <=1.20.1 {
	/*@Shadow
	public abstract boolean hasTag();

	@Shadow
	public abstract CompoundTag getTag();
	*///?} else {
	@Shadow
	@Final
    private PatchedDataComponentMap components;
	//?}

	@Unique
	private boolean neruina$errored = false;

	@Unique
	private UUID neruina$tickingEntryId = null;

	@Override
	public boolean neruina$isErrored() {
		return neruina$errored;
	}

	@Override
	public void neruina$setErrored() {
		neruina$errored = true;
		neruina$updateData();
	}

	@Override
	public void neruina$clearErrored() {
		neruina$errored = false;
		neruina$updateData();
	}

	@Override
	public void neruina$setTickingEntryId(UUID uuid) {
		neruina$tickingEntryId = uuid;
		neruina$updateData();
	}

	@Override
	public UUID neruina$getTickingEntryId() {
		return neruina$tickingEntryId;
	}

	//? if <=1.20.1 {
	/*@Unique
	private void neruina$updateData() {
		if (neruina$errored) {
			CompoundTag tag = new CompoundTag();
			tag.putBoolean("neruina$errored", true);
			if(neruina$tickingEntryId != null) {
				tag.putUUID("neruina$tickingEntryId", neruina$tickingEntryId);
			}
		}
	}

	@Inject(
			method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V",
			at = @At("TAIL")
	)
	private void readErroredFromNbt(CompoundTag itemNbt, CallbackInfo ci) {
		if(hasTag()) {
			CompoundTag tag = getTag();
			if (tag.contains("neruina$errored")) {
				neruina$errored = tag.getBoolean("neruina$errored");
			}
			if (tag.contains("neruina$tickingEntryId")) {
				neruina$tickingEntryId = tag.getUUID("neruina$tickingEntryId");
			}
		}
	}
	*///?} else {
	@Unique
	private void neruina$updateData() {
		CompoundTag tag = new CompoundTag();
		if (neruina$errored) {
			tag.putBoolean("neruina$errored", true);
		}
		if (neruina$tickingEntryId != null) {
			tag.putString("neruina$tickingEntryId", neruina$tickingEntryId.toString());
		}
		CustomData data = CustomData.of(tag);
		DataComponentPatch.Builder builder = DataComponentPatch.builder()
				.set(DataComponents.CUSTOM_DATA, data);
		components.applyPatch(builder.build());
	}

	@Inject(
			//~ if >=26.1 'Lnet/minecraft/world/level/ItemLike' -> 'Lnet/minecraft/core/Holder'
			method = "<init>(Lnet/minecraft/core/Holder;ILnet/minecraft/core/component/PatchedDataComponentMap;)V",
			at = @At("TAIL")
	)
	private void readErroredFromComponents(CallbackInfo ci) {
		CustomData data = components.get(DataComponents.CUSTOM_DATA);
		if (data == null) return;

		CompoundTag tag = data.copyTag();
		//? if <=1.21.1 {
    /*neruina$errored = tag.getBoolean("neruina$errored");
    if (tag.contains("neruina$tickingEntryId")) {
      neruina$tickingEntryId = tag.getUUID("neruina$tickingEntryId");
    }
    *///?} else {
		neruina$errored = tag.getBooleanOr("neruina$errored", false);
		neruina$tickingEntryId = tag.getString("neruina$tickingEntryId").map(UUID::fromString).orElse(null);
		//?}
	}
	//?}
}