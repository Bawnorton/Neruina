package com.bawnorton.neruina.mixin.forge.noseenotick;

import com.bawnorton.neruina.annotation.ConditionalMixin;
import com.bawnorton.neruina.annotation.VersionedMixin;
import com.bawnorton.neruina.handler.NeruinaTickHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(value = World.class, priority = 1500)
@ConditionalMixin(modids = "noseenotick")
@VersionedMixin(">=1.19")
public abstract class WorldMixin {
    @Inject(method = "shouldUpdatePostDeath", at = @At("HEAD"), cancellable = true)
    public void shouldUpdatePostDeath(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if(NeruinaTickHandler.isErrored(entity)) {
            cir.setReturnValue(false);
        }
    }

    @SuppressWarnings({"unused", "MixinAnnotationTarget"})
    @WrapOperation(method = "tickEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/TickOptimizer;entityTicking(Ljava/util/function/Consumer;Lnet/minecraft/entity/Entity;Lnet/minecraft/world/World;Lnet/minecraft/util/math/random/Random;)V"))
    private void catchTickingEntities(Consumer<Entity> consumer, Entity entity, World world, Random random, Operation<Void> original) {
        NeruinaTickHandler.safelyTickEntities$notTheCauseOfTickLag(consumer, entity, world, random, original);
    }
}
