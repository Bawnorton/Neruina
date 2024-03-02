package com.bawnorton.neruina.mixin.catchers;

import com.bawnorton.neruina.Neruina;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
    @Shadow @Final public DefaultedList<ItemStack> main;

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void removeErroredStatusOnInvInit(CallbackInfo ci) {
        main.forEach(stack -> {
            if(stack.getOrDefault(Neruina.getErroredComponent(), false)) {
                Neruina.getInstance().getTickHandler().removeErrored(stack);
            }
        });
    }
}
