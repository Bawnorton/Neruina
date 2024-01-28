package com.bawnorton.neruina.mixin.dev;

import com.bawnorton.neruina.annotation.DevOnlyMixin;
import net.minecraft.item.CompassItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CompassItem.class)
@DevOnlyMixin
public abstract class TestItemStackCrashMixin {
    /*? if dev {*/
    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void throwTestException(CallbackInfo ci) {
        throw new RuntimeException("Test exception");
    }
    /*?}*/
}
