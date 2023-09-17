package com.bawnorton.neruina.mixin.forge.itshallnottick.v118;

import com.bawnorton.neruina.annotation.ConditionalMixin;
import com.bawnorton.neruina.annotation.VersionedMixin;
import com.bawnorton.neruina.handler.NeruinaTickHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Pseudo
@Mixin(targets = "dev.wuffs.itshallnottick.TickOptimizer", remap = false)
@ConditionalMixin(modid = "itshallnottick")
@VersionedMixin("<=1.18.2")
public abstract class TickOptimizerMixin {
    @SuppressWarnings("unused")
    @WrapOperation(method = "handleGuardEntityTick", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"))
    private static void catchTickingEntities(Consumer<Entity> consumer, Object param, Operation<Void> original) {
        NeruinaTickHandler.safelyTickEntities$notTheCauseOfTickLag(consumer, param, original);
    }
}