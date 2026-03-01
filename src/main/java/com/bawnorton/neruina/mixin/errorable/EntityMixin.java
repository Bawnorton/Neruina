package com.bawnorton.neruina.mixin.errorable;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.extend.Errorable;
import com.bawnorton.neruina.handler.MessageHandler;
import com.bawnorton.neruina.util.TickingEntry;
import com.bawnorton.neruina.version.Texter;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

//? if >=1.21.6 {
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?}

@Mixin(Entity.class)
abstract class EntityMixin implements Errorable {
	@Shadow
	private Level level;
	@Unique
	private boolean neruina$errored = false;
	@Unique
	private UUID neruina$tickingEntryId = null;

	@Shadow
	public abstract Component getName();

	@Shadow
	public abstract Level level();

	@Override
	public boolean neruina$isErrored() {
		return neruina$errored;
	}

	@Override
	public void neruina$setErrored() {
		neruina$errored = true;
	}

	@Override
	public void neruina$clearErrored() {
		neruina$errored = false;
	}

	@Override
	public void neruina$setTickingEntryId(UUID uuid) {
		neruina$tickingEntryId = uuid;
	}

	@Override
	public UUID neruina$getTickingEntryId() {
		return neruina$tickingEntryId;
	}

	//? if <=1.21.5 {
	/*@Inject(
			method = "saveWithoutId",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"
			)
	)
	private void writeErrored(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
		if (neruina$errored) {
			tag.putBoolean("neruina$errored", true);
		}
		if (neruina$tickingEntryId != null) {
			tag.putString("neruina$tickingEntryId", neruina$tickingEntryId.toString());
		}
	}

	@Inject(
			method = "load",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;setAirSupply(I)V"
			)
	)
	private void loadAdditional(CompoundTag tag, CallbackInfo ci) {
		//? if <=1.21.1 {
    /^neruina$errored = tag.getBoolean("neruina$errored");
    if (tag.contains("neruina$tickingEntryId")) {
			try {
        neruina$tickingEntryId = UUID.fromString(tag.getString("neruina$tickingEntryId"));
			} catch (IllegalArgumentException e) {
				neruina$tickingEntryId = null;
				neruina$clearErrored();
			}
    }
    ^///?} else {
		neruina$errored = tag.getBooleanOr("neruina$errored", false);
		try {
			neruina$tickingEntryId = tag.getString("neruina$tickingEntryId").map(UUID::fromString).orElse(null);
		} catch (IllegalArgumentException e) {
			neruina$tickingEntryId = null;
			neruina$clearErrored();
		}
		//?}
	}
	*///?} else {
	@Inject(
			method = "saveWithoutId",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;addAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueOutput;)V"
			)
	)
	private void writeErroredToNbt(ValueOutput output, CallbackInfo ci) {
		if (neruina$errored) {
			output.putBoolean("neruina$errored", true);
		}
		if (neruina$tickingEntryId != null) {
			output.putString("neruina$tickingEntryId", neruina$tickingEntryId.toString());
		}
	}

	@Inject(
			method = "load",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;setAirSupply(I)V"
			)
	)
	private void readErroredFromNbt(ValueInput input, CallbackInfo ci) {
		neruina$errored = input.getBooleanOr("neruina$errored", false);
		try {
			neruina$tickingEntryId = input.getString("neruina$tickingEntryId").map(UUID::fromString).orElse(null);
		} catch (IllegalArgumentException e) {
			neruina$tickingEntryId = null;
			neruina$clearErrored();
		}
	}
	//?}

	@ModifyReturnValue(
			method = {
					"isInvulnerableTo",
					"method_64421",
					"isInvulnerableToBase"
			},
			at = @At("RETURN")
	)
	private boolean ignoreDamageWhenErrored(boolean original, @Local(argsOnly = true) DamageSource source) {
		if (original) return true;

		if (neruina$errored && neruina$tickingEntryId != null) {
			if (source.getEntity() instanceof ServerPlayer player) {
				TickingEntry entry = Neruina.getInstance().getTickHandler().getTickingEntry(neruina$tickingEntryId);
				MessageHandler messageHandler = Neruina.getInstance().getMessageHandler();
				if (entry == null) {
					messageHandler.sendToPlayer(
							player,
							Texter.concatDelimited(
									Texter.LINE_BREAK,
									Texter.translatable("neruina.suspended.entity", getName().getString()),
									Texter.translatable("neruina.suspended.entity.untracked")
							),
							messageHandler.generateEntityActions(player, (Entity) (Object) this),
							messageHandler.generateInfoAction()
					);
				} else {
					messageHandler.sendToPlayer(
							player,
							Texter.translatable("neruina.suspended.entity", getName().getString()),
							messageHandler.generateEntityActions(player, (Entity) (Object) this),
							messageHandler.generateResourceActions(player, entry)
					);
				}
			}
			return source != level.damageSources().genericKill();
		}
		return false;
	}
}
