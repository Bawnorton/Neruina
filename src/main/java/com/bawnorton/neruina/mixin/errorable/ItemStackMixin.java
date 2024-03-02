package com.bawnorton.neruina.mixin.errorable;

import com.bawnorton.neruina.Neruina;
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

/*? if >=1.20.2 {*/
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMapImpl;
/*? }*/

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements Errorable {
    /*? if >=1.20.2 {*/
    @Shadow @Final
    ComponentMapImpl components;
    /*? } else {*//*
    @Shadow public abstract NbtCompound getOrCreateNbt();

    @Shadow @Nullable
    public abstract NbtCompound getNbt();

    @Shadow public abstract boolean hasNbt();
    *//*? }*/

    @Unique
    private boolean neruina$errored = false;

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
    public void neruina$setTickingEntry(UUID uuid) {
    }

    @Override
    public UUID neruina$getTickingEntry() {
        return null;
    }

    /*? if >=1.20.2 {*/
    @Unique
    private void neruina$updateData() {
        ComponentChanges changes = ComponentChanges.builder()
                .add(Neruina.getErroredComponent(), neruina$errored)
                .build();
        components.applyChanges(changes);
    }

    @Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;ILnet/minecraft/component/ComponentMapImpl;)V", at = @At("TAIL"))
    private void readErroredFromComponents(ItemConvertible item, int count, ComponentMapImpl components, CallbackInfo ci) {
        if(components.contains(Neruina.getErroredComponent())) {
            neruina$errored = components.getOrDefault(Neruina.getErroredComponent(), false);
        }
    }
    /*? } else {*//*
    @Unique
    private void neruina$updateData() {
        NbtCompound nbt = getOrCreateNbt();
        if (neruina$errored) {
            nbt.putBoolean("neruina$errored", true);
        } else {
            nbt.remove("neruina$errored");
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
        }
    }
    *//*? }*/
}
