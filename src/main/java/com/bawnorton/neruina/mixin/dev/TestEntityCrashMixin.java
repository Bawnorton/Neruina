package com.bawnorton.neruina.mixin.dev;

import com.bawnorton.neruina.annotation.DevOnlyMixin;
import net.minecraft.entity.mob.ZombieEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZombieEntity.class)
@DevOnlyMixin
public abstract class TestEntityCrashMixin {
    /*? if dev {*/
    @Inject(method = "tick", at = @At("HEAD"))
    private void throwTestException(CallbackInfo ci) {
        throw new RuntimeException("Test exception");
    }
    /*?}*/
}
