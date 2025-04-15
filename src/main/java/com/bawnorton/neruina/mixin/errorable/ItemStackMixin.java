package com.bawnorton.neruina.mixin.errorable;

import com.bawnorton.neruina.extend.Errorable;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.UUID;

//? if >=1.20.2 {
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
//? if >1.21.2 {
import net.minecraft.component.MergedComponentMap;
//?} else {
/*import net.minecraft.component.ComponentMapImpl;
*///?}
//?}

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements Errorable {
    //? if >1.21.1 {
    @Shadow @Final
    MergedComponentMap components;
    //?} elif >=1.20.2 {
    /*@Shadow @Final
    ComponentMapImpl components;
    *///?} else {
    /*@Shadow public abstract NbtCompound getOrCreateNbt();

    @Shadow @Nullable
    public abstract NbtCompound getNbt();

    @Shadow public abstract boolean hasNbt();
    *///?}

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

    //? if >=1.20.2 {
    @Unique
    private void neruina$updateData() {
        NbtCompound nbt = new NbtCompound();
        nbt.putBoolean("neruina$errored", neruina$errored);
        if(neruina$tickingEntryId != null) {
            //? if >1.21.4 {
            /*nbt.putString("neruina$tickingEntryId", neruina$tickingEntryId.toString());
            *///?} else {
            nbt.putUuid("neruina$tickingEntryId", neruina$tickingEntryId);
            //?}
        }
        ComponentChanges.Builder builder = ComponentChanges.builder()
                .add(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        components.applyChanges(builder.build());
    }

    //? if >1.21.2 {
    @Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;ILnet/minecraft/component/MergedComponentMap;)V", at = @At("TAIL"))
    private void readErroredFromComponents(ItemConvertible item, int count, MergedComponentMap components, CallbackInfo ci) {
    //?} else {
    /*@Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;ILnet/minecraft/component/ComponentMapImpl;)V", at = @At("TAIL"))
    private void readErroredFromComponents(ItemConvertible item, int count, ComponentMapImpl components, CallbackInfo ci) {
    *///?}
        NbtComponent nbtComponent = components.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent == null) return;

        NbtCompound tag = nbtComponent.copyNbt();

        //? if >1.21.4 {
        /*neruina$errored = tag.getBoolean("neruina$errored", false);
        neruina$tickingEntryId = tag.getString("neruina$tickingEntryId").map(UUID::fromString).orElse(null);
        *///?} else {
        neruina$errored = tag.getBoolean("neruina$errored");
        if(tag.contains("neruina$tickingEntryId")) {
            neruina$tickingEntryId = tag.getUuid("neruina$tickingEntryId");
        }
        //?}
    }
    //?} else {
    /*@Unique
    private void neruina$updateData() {
        NbtCompound nbt = getOrCreateNbt();
        if (neruina$errored) {
            nbt.putBoolean("neruina$errored", true);
            if (neruina$tickingEntryId != null) {
                nbt.putUuid("neruina$tickingEntryId", neruina$tickingEntryId);
            }
        } else {
            nbt.remove("neruina$errored");
            nbt.remove("neruina$tickingEntryId");
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("TAIL"))
    private void readErroredFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if(hasNbt()) {
            NbtCompound tag = getNbt();
            assert tag != null;
            if (tag.contains("neruina$errored")) {
                neruina$errored = tag.getBoolean("neruina$errored");
            }
            if (tag.contains("neruina$tickingEntryId")) {
                neruina$tickingEntryId = tag.getUuid("neruina$tickingEntryId");
            }
        }
    }
    *///?}
}
