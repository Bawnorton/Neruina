package com.bawnorton.neruina.mixin.catchers;

import com.bawnorton.neruina.Neruina;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=1.20.2 {
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
//?}

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
    @Shadow @Final
    //? if >1.21.4 {
    /*private DefaultedList<ItemStack> main;
    *///?} else {
    public DefaultedList<ItemStack> main;
    //?}


    //? if >1.21.4 {
    /*@WrapOperation(
            method = "updateItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;inventoryTick(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/EquipmentSlot;)V"
            )
    )
    private void catchTickingItemStack$notTheCauseOfTickLag(ItemStack instance, World world, Entity entity, EquipmentSlot slot, Operation<Void> original, @Local(ordinal = 0) int slotIndex) {
        Neruina.getInstance().getTickHandler().safelyTickItemStack(instance, world, entity, slot, slotIndex, original);
    }
    *///?} else {
    @WrapOperation(
            method = "updateItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;inventoryTick(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;IZ)V"
            )
    )
    private void catchTickingItemStack$notTheCauseOfTickLag(ItemStack instance, World world, Entity entity, int slot, boolean selected, Operation<Void> original) {
        Neruina.getInstance().getTickHandler().safelyTickItemStack(instance, world, entity, slot, selected, original);
    }
    //?}

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void removeErroredStatusOnInvInit(CallbackInfo ci) {
        //? if >=1.20.2 {
        main.forEach(stack -> {
            NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
            if (component == null) return;

            NbtCompound nbt = component.copyNbt();
            //? if >=1.21.4 {
            /*if (nbt.getBoolean("neruina$errored", false)) {
            *///?} else {
            if(nbt.getBoolean("neruina$errored")) {
            //?}
                Neruina.getInstance().getTickHandler().removeErrored(stack);
            }
        });
        //?} else {
        /*main.forEach(stack -> {
            if(stack.hasNbt()) {
                NbtCompound nbt = stack.getNbt();
                if(nbt.getBoolean("neruina$errored")) {
                    Neruina.getInstance().getTickHandler().removeErrored(stack);
                }
            }
        });
        *///?}
    }
}
