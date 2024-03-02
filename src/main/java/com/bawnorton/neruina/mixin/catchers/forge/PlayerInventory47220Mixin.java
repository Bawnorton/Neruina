package com.bawnorton.neruina.mixin.catchers.forge;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.annotation.ModLoaderMixin;
import com.bawnorton.neruina.annotation.Version;
import com.bawnorton.neruina.platform.ModLoader;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerInventory.class)
@ModLoaderMixin(value = ModLoader.FORGE, version = @Version(min = "47.2.20"))
public abstract class PlayerInventory47220Mixin {
    @WrapOperation(method = "updateItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;onInventoryTick(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;II)V"))
    private void catchTickingItemStack$notTheCauseOfTickLag(ItemStack instance, World world, PlayerEntity playerEntity, int slot, int selected, Operation<Void> original) {
        Neruina.getInstance().getTickHandler().safelyTickItemStack(instance, world, playerEntity, slot, selected, original);
    }
}
