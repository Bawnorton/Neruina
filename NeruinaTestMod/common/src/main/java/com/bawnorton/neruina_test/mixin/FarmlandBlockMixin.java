package com.bawnorton.neruina_test.mixin;

import com.bawnorton.neruina_test.TestException;
import net.minecraft.block.FarmlandBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmlandBlock.class)
public abstract class FarmlandBlockMixin {
    @Inject(method = "randomTick", at = @At("HEAD"), require = 0)
    private void throwTestExceptionV119(CallbackInfo ci) {
        throw TestException.create();
    }
}
