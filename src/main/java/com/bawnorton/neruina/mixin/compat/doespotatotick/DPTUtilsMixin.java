package com.bawnorton.neruina.mixin.compat.doespotatotick;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import com.bawnorton.neruina.annotation.ConditionalMixin;

/*? if <1.19 { *//*
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.function.Consumer;
*//*? } */

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
