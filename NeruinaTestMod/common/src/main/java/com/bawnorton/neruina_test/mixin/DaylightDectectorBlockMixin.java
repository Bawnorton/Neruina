package com.bawnorton.neruina_test.mixin;

import com.bawnorton.neruina_test.TestException;
import net.minecraft.block.DaylightDetectorBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DaylightDetectorBlock.class)
public abstract class DaylightDectectorBlockMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private static void throwTestException(CallbackInfo ci) {
        throw TestException.create();
    }
}
