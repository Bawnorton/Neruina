package com.bawnorton.neruina.mixin.catchers;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.annotation.ModLoaderMixin;
import com.bawnorton.neruina.platform.ModLoader;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
@ModLoaderMixin({ModLoader.FABRIC, ModLoader.NEOFORGE})
public abstract class PlayerInventoryMixin {
    @Shadow @Final public DefaultedList<ItemStack> main;

    @WrapOperation(method = "updateItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;inventoryTick(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;IZ)V"))
    private void catchTickingItemStack$notTheCauseOfTickLag(ItemStack instance, World world, Entity entity, int slot, boolean selected, Operation<Void> original) {
        Neruina.getInstance().getTickHandler().safelyTickItemStack(instance, world, entity, slot, selected, original);
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void removeErroredStatusOnInvInit(CallbackInfo ci) {
        main.forEach(stack -> {
            if(stack.getOrDefault(Neruina.getErroredComponent(), false))  {
                Neruina.getInstance().getTickHandler().removeErrored(stack);
            }
        });
    }
}
