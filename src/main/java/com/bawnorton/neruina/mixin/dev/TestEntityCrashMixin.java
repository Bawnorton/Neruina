package com.bawnorton.neruina.mixin.dev;

import com.bawnorton.neruina.annotation.DevOnlyMixin;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CowEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnimalEntity.class)
@DevOnlyMixin
public abstract class TestEntityCrashMixin {
    /*? if dev {*/
    @Inject(method = "mobTick", at = @At("HEAD"))
    private void throwTestException(CallbackInfo ci) {
        if (((Object) this) instanceof CowEntity) {
            throw new RuntimeException("Test exception");
        }
    }
    /*?}*/
}
