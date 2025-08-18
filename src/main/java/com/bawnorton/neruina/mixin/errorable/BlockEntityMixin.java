package com.bawnorton.neruina.mixin.errorable;

import com.bawnorton.neruina.extend.Errorable;
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.UUID;

//? if 1.21.8 {
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?}

@MixinEnvironment
@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin implements Errorable {
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
            method = "saveAdditional",
            at = @At("HEAD")
    )
    private void writeErrored(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (neruina$errored) {
            tag.putBoolean("neruina$errored", true);
        }
        if (neruina$tickingEntryId != null) {
            tag.putString("neruina$tickingEntryId", neruina$tickingEntryId.toString());
        }
    }

    @Inject(
            method = "loadAdditional",
            at = @At("TAIL")
    )
    private void loadAdditional(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        //? if 1.21.1 {
        /^neruina$errored = tag.getBoolean("neruina$errored");
        if (tag.contains("neruina$tickingEntryId")) {
            neruina$tickingEntryId = UUID.fromString(tag.getString("neruina$tickingEntryId"));
        }
        ^///?} else {
        neruina$errored = tag.getBooleanOr("neruina$errored", false);
        neruina$tickingEntryId = tag.getString("neruina$tickingEntryId").map(UUID::fromString).orElse(null);
        //?}
    }
    *///?} else {
    @Inject(
            method = "saveAdditional",
            at = @At("HEAD")
    )
    private void writeErroredToOutput(ValueOutput output, CallbackInfo ci) {
        if (neruina$errored) {
            output.putBoolean("neruina$errored", true);
        }
        if (neruina$tickingEntryId != null) {
            output.putString("neruina$tickingEntryId", neruina$tickingEntryId.toString());
        }
    }

    @Inject(
            method = "loadAdditional",
            at = @At("TAIL")
    )
    private void readErroredFromNbt(ValueInput input, CallbackInfo ci) {
        neruina$errored = input.getBooleanOr("neruina$errored", false);
        neruina$tickingEntryId = input.getString("neruina$tickingEntryId").map(UUID::fromString).orElse(null);
    }
    //?}
}
