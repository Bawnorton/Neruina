package com.bawnorton.neruina.mixin.test;

import com.bawnorton.neruina.util.annotation.DevOnly;
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@DevOnly
@MixinEnvironment
@Mixin(Zombie.class)
public abstract class ZombieMixin {
	@Inject(
			method = "tick",
			at = @At("HEAD")
	)
	private void crash(CallbackInfo ci) {
		throw new RuntimeException();
	}
}
