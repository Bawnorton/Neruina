package com.bawnorton.neruina.mixin.compat.doespotatotick;

import com.bawnorton.neruina.annotation.ConditionalMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "com.teampotato.does_potato_tick.util.DPTUtils")
@ConditionalMixin(modids = "does_potato_tick")
public abstract class DPTUtilsMixin {
    /*? if <1.19 { *//*
    @Inject(method = "handleGuardEntityTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getType()Lnet/minecraft/entity/EntityType;"))
    private static void letNeruinaHandleIt(Consumer<Entity> consumer, Entity entity, CallbackInfo ci, @Local Throwable e) throws Throwable {
        throw e;
    }
    *//*? } */
}
