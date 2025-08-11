package com.bawnorton.neruina.mixin.test;

import com.bawnorton.neruina.util.annotation.DevOnly;
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import net.minecraft.world.level.block.FarmBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@DevOnly
@MixinEnvironment
@Mixin(FarmBlock.class)
public abstract class FarmBlockMixin {
    @Inject(
            method = "randomTick",
            at = @At("HEAD")
    )
    private void crash(CallbackInfo ci) {
        throw new RuntimeException();
    }
}
