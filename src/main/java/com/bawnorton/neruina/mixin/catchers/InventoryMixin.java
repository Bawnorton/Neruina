package com.bawnorton.neruina.mixin.catchers;

import com.bawnorton.neruina.Neruina;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@MixinEnvironment
@Mixin(Inventory.class)
public abstract class InventoryMixin {
	@Shadow
	@Final
			//? if 1.21.1 {
	/*public NonNullList<ItemStack> items;
	 *///?} else {
	private NonNullList<ItemStack> items;
	//?}

	//? if 1.21.1 {
    /*@WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;inventoryTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;IZ)V"
            )
    )
    private void catchTickingItemStack$notTheCauseOfTickLag(ItemStack instance, Level level, Entity entity, int i, boolean b, Operation<Void> original) {
        Neruina.getInstance().getTickHandler().safelyTickItemStack(instance, level, entity, i, b, original);
    }
    *///?} else {
	@WrapOperation(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/ItemStack;inventoryTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/EquipmentSlot;)V"
			)
	)
	private void catchTickingItemStack$notTheCauseOfTickLag(ItemStack instance, Level level, Entity entity, EquipmentSlot slot, Operation<Void> original, @Local(ordinal = 0) int slotIndex) {
		Neruina.getInstance().getTickHandler().safelyTickItemStack(instance, level, entity, slot, slotIndex, original);
	}
	//?}

	@Inject(method = "load", at = @At("TAIL"))
	private void removeErroredStatusOnInvInit(CallbackInfo ci) {
		items.forEach(stack -> {
			CustomData data = stack.get(DataComponents.CUSTOM_DATA);
			if (data == null) return;

			CompoundTag tag = data.copyTag();
			//? if 1.21.1 {
            /*if (tag.getBoolean("neruina$errored")) {
                Neruina.getInstance().getTickHandler().removeErrored(stack);
            }
            *///?} else {
			if (tag.getBoolean("neruina$errored").orElse(false)) {
				Neruina.getInstance().getTickHandler().removeErrored(stack);
			}
			//?}
		});
	}
}
