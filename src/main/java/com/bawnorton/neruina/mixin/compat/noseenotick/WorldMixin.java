package com.bawnorton.neruina.mixin.compat.noseenotick;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.annotation.ConditionalMixin;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import java.util.function.Consumer;

@Mixin(value = World.class, priority = 1500)
@ConditionalMixin(modids = "noseenotick")
public abstract class WorldMixin {
    @ModifyReturnValue(method = "shouldUpdatePostDeath", at = @At("RETURN"))
    private boolean shouldUpdatePostDeath(boolean original, Entity entity) {
        if (original) {
            return !Neruina.TICK_HANDLER.isErrored(entity);
        }

        return false;
    }

    @SuppressWarnings({"MixinAnnotationTarget", "InvalidInjectorMethodSignature", "UnresolvedMixinReference"})
    /*? if >=1.19 {*/
    @WrapOperation(method = "tickEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/TickOptimizer;entityTicking(Ljava/util/function/Consumer;Lnet/minecraft/entity/Entity;Lnet/minecraft/world/World;Lnet/minecraft/util/math/random/Random;)V"))
    /*?} else {*//*
    @WrapOperation(method = "tickEntity", at = @At(value = "INVOKE", target = "Lcom/sargerasarm/noseenotick/TickOptimizer;entityTicking(Ljava/util/function/Consumer;Lnet/minecraft/entity/Entity;Lnet/minecraft/world/World;Ljava/util/Random;)V"))
    *//*?}*/
    private void catchTickingEntities(Consumer<Entity> consumer, Entity entity, World world, @Coerce Object random, Operation<Void> original) {
        Neruina.TICK_HANDLER.safelyTickEntities(consumer, entity, world, random, original);
    }
}
