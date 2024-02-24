package com.bawnorton.neruina.mixin.errorable;

import com.bawnorton.neruina.extend.Errorable;
import com.llamalad7.mixinextras.sugar.Local;
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
    public void neruina$setTickingEntry(UUID uuid) {
        neruina$tickingEntryId = uuid;
    }

    @Override
    public UUID neruina$getTickingEntry() {
        return neruina$tickingEntryId;
    }

    @Inject(method = "writeNbt", at = @At("HEAD"))
    private void writeErroredToNbt(CallbackInfo ci, @Local(argsOnly = true) NbtCompound nbt) {
        if (neruina$errored) {
            nbt.putBoolean("neruina$errored", true);
        }
        if (neruina$tickingEntryId != null) {
            nbt.putUuid("neruina$tickingEntryId", neruina$tickingEntryId);
        }
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void readErroredFromNbt(CallbackInfo ci, @Local(argsOnly = true) NbtCompound nbt) {
        if (nbt.contains("neruina$errored")) {
            neruina$errored = nbt.getBoolean("neruina$errored");
        }
        if (nbt.contains("neruina$tickingEntryId")) {
            neruina$tickingEntryId = nbt.getUuid("neruina$tickingEntryId");
        }
    }
}
