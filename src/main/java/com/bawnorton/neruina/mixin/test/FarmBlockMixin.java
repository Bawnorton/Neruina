package com.bawnorton.neruina.mixin.test;

import com.bawnorton.neruina.util.annotation.DevOnly;
//~ if >=26.1 'FarmBlock' -> 'FarmlandBlock'
import net.minecraft.world.level.block.FarmlandBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@DevOnly
//~ if >=26.1 'FarmBlock' -> 'FarmlandBlock'
@Mixin(FarmlandBlock.class)
public abstract class FarmBlockMixin {
	@Inject(
			method = "randomTick",
			at = @At("HEAD")
	)
	private void crash(CallbackInfo ci) {
		throw new RuntimeException();
	}
}
