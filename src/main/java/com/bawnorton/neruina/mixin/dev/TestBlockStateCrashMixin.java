package com.bawnorton.neruina.mixin.dev;

import com.bawnorton.neruina.annotation.DevOnlyMixin;
import net.minecraft.block.FarmlandBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmlandBlock.class)
@DevOnlyMixin
public abstract class TestBlockStateCrashMixin {
    /*? if dev {*/
    @Inject(method = "randomTick", at = @At("HEAD"))
    private void throwTestException(CallbackInfo ci) {
        throw new RuntimeException("Test exception");
    }
    /*?}*/
}
