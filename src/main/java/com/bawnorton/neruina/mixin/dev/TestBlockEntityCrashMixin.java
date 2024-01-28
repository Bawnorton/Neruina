package com.bawnorton.neruina.mixin.dev;

import com.bawnorton.neruina.annotation.DevOnlyMixin;
import net.minecraft.block.DaylightDetectorBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DaylightDetectorBlock.class)
@DevOnlyMixin
public abstract class TestBlockEntityCrashMixin {
    /*? if dev {*/
    @Inject(method = "tick", at = @At("HEAD"))
    private static void throwTestException(CallbackInfo ci) {
        throw new RuntimeException("Test exception");
    }
    /*?}*/
}
