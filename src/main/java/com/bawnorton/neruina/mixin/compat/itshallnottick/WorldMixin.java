package com.bawnorton.neruina.mixin.compat.itshallnottick;

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
@ConditionalMixin(modids = "itshallnottick")
public abstract class WorldMixin {
    @Inject(method = "shouldUpdatePostDeath", at = @At("HEAD"), cancellable = true)
    private void shouldUpdatePostDeath(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (Neruina.TICK_HANDLER.isErrored(entity)) {
            cir.setReturnValue(false);
        }
    }

    @SuppressWarnings({"UnresolvedMixinReference", "MixinAnnotationTarget", "InvalidInjectorMethodSignature"})
    /*? if >=1.19 {*/
    @WrapOperation(method = "tickEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/TickOptimizer;entityTicking(Ljava/util/function/Consumer;Lnet/minecraft/entity/Entity;Lnet/minecraft/world/World;Lnet/minecraft/util/math/random/Random;)V"))
    /*?} else {*//*
    @WrapOperation(method = "tickEntity", at = @At(value = "INVOKE", target = "Ldev/wuffs/itshallnottick/TickOptimizer;entityTicking(Ljava/util/function/Consumer;Lnet/minecraft/entity/Entity;Lnet/minecraft/world/World;Ljava/util/Random;)V"))
    *//*?}*/
    private void catchTickingEntities(Consumer<Entity> consumer, Entity entity, World world, @Coerce Object random, Operation<Void> original) {
        Neruina.TICK_HANDLER.safelyTickEntities(consumer, entity, world, random, original);
    }
}
