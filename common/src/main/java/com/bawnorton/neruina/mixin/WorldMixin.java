package com.bawnorton.neruina.mixin;

import com.bawnorton.neruina.annotation.ConditionalMixin;
import com.bawnorton.neruina.handler.NeruinaTickHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(value = World.class, priority = 1500)
@ConditionalMixin(modids = {"noseenotick", "itshallnottick"}, applyIfPresent = false)
public abstract class WorldMixin {
    @Inject(method = "shouldUpdatePostDeath", at = @At("HEAD"), cancellable = true)
    public void shouldUpdatePostDeath(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if(NeruinaTickHandler.isErrored(entity)) {
            cir.setReturnValue(false);
        }
    }

    @WrapOperation(method = "tickEntity", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"))
    public void catchTickingEntities(Consumer<Entity> instance, Object entity, Operation<Void> original) {
        NeruinaTickHandler.safelyTickEntities$notTheCauseOfTickLag(instance, (Entity) entity, original);
    }
}
