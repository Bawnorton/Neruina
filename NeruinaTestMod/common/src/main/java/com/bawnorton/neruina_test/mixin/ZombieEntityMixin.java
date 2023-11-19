package com.bawnorton.neruina_test.mixin;

import com.bawnorton.neruina_test.TestException;
import net.minecraft.entity.mob.ZombieEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZombieEntity.class)
public abstract class ZombieEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void throwTestException(CallbackInfo ci) {
        throw TestException.create();
    }
}
