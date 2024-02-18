package com.bawnorton.neruina.mixin.compat.doespotatotick;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.annotation.ConditionalMixin;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.function.Consumer;

@Mixin(value = World.class, priority = 1500)
@ConditionalMixin(modids = "does_potato_tick")
public abstract class WorldMixin {
    @Inject(method = "shouldUpdatePostDeath", at = @At("HEAD"), cancellable = true)
    private void shouldUpdatePostDeath(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (Neruina.TICK_HANDLER.isErrored(entity)) {
            cir.setReturnValue(false);
        }
    }

    /*? if >=1.19 {*/
    @WrapOperation(method = "tickEntity", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", remap = false))
    /*?} else {*//*
    @SuppressWarnings({"UnresolvedMixinReference", "MixinAnnotationTarget", "InvalidInjectorMethodSignature"})
    @WrapOperation(method = "tickEntity", at = @At(value = "INVOKE", target = "Lcom/teampotato/does_potato_tick/util/DPTUtils;handleGuardEntityTick(Ljava/util/function/Consumer;Lnet/minecraft/entity/Entity;)V"))
    *//*?}*/
    private void catchTickingEntities(Consumer<Object> consumer, @Coerce Object entity, Operation<Void> original) {
        Neruina.TICK_HANDLER.safelyTickEntities(consumer, (Entity) entity, original);
    }
}
