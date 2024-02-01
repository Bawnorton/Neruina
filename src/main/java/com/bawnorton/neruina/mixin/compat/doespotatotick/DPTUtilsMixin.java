package com.bawnorton.neruina.mixin.compat.doespotatotick;

import com.bawnorton.neruina.annotation.ConditionalMixin;
import com.llamalad7.mixinextras.sugar.Local;
import com.teampotato.does_potato_tick.util.DPTUtils;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.function.Consumer;

@Mixin(DPTUtils.class)
@ConditionalMixin(modids = "does_potato_tick")
public abstract class DPTUtilsMixin {
    /*? if <1.19 { *//*
    @Inject(method = "handleGuardEntityTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getType()Lnet/minecraft/entity/EntityType;"))
    private static void letNeruinaHandleIt(Consumer<Entity> consumer, Entity entity, CallbackInfo ci, @Local Throwable e) throws Throwable {
        throw e;
    }
    *//*? } */
}
