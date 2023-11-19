package com.bawnorton.neruina_test.mixin;

import com.bawnorton.neruina_test.TestException;
import net.minecraft.item.CompassItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CompassItem.class)
public abstract class CompassItemMixin {
    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void throwTestException(CallbackInfo ci) {
        throw TestException.create();
    }
}
