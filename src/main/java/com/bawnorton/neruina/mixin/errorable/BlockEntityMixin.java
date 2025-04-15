package com.bawnorton.neruina.mixin.errorable;

import com.bawnorton.neruina.extend.Errorable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.UUID;

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

    @Inject(method = "writeNbt", at = @At("HEAD"))
    //? if >=1.20.2 {
    private void writeErroredToNbt(NbtCompound nbt, net.minecraft.registry.RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
    //?} else {
    /*private void writeErroredToNbt(NbtCompound nbt, CallbackInfo ci) {
    *///?}
        if (neruina$errored) {
            nbt.putBoolean("neruina$errored", true);
        }
        if (neruina$tickingEntryId != null) {
            //? if >1.21.4 {
            /*nbt.putString("neruina$tickingEntryId", neruina$tickingEntryId.toString());
            *///?} else {
            nbt.putUuid("neruina$tickingEntryId", neruina$tickingEntryId);
            //?}
        }
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    //? if >=1.20.2 {
    private void readErroredFromNbt(NbtCompound nbt, net.minecraft.registry.RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
    //?} else {
    /*private void readErroredFromNbt(NbtCompound nbt, CallbackInfo ci) {
    *///?}
        //? if >1.21.4 {
        /*neruina$errored = nbt.getBoolean("neruina$errored", false);
        neruina$tickingEntryId = nbt.getString("neruina$tickingEntryId").map(UUID::fromString).orElse(null);
        *///?} else {
        if (nbt.contains("neruina$errored")) {
            neruina$errored = nbt.getBoolean("neruina$errored");
        }
        if (nbt.contains("neruina$tickingEntryId")) {
            neruina$tickingEntryId = nbt.getUuid("neruina$tickingEntryId");
        }
        //?}
    }
}
