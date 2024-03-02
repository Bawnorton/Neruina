package com.bawnorton.neruina.mixin.catchers.neoforge;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.annotation.ModLoaderMixin;
import com.bawnorton.neruina.platform.ModLoader;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerInventory.class)
@ModLoaderMixin(ModLoader.NEOFORGE)
public abstract class PlayerInventoryMixin {
    @WrapOperation(method = "updateItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;inventoryTick(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;IZ)V"))
    private void catchTickingItemStack$notTheCauseOfTickLag(ItemStack instance, World world, Entity entity, int slot, boolean selected, Operation<Void> original) {
        Neruina.getInstance().getTickHandler().safelyTickItemStack(instance, world, entity, slot, selected, original);
    }
}
