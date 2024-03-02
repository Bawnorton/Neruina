package com.bawnorton.neruina.mixin.catchers;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.annotation.ConditionalMixin;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import java.util.function.Consumer;

@Mixin(value = World.class, priority = 1500)
@ConditionalMixin(modids = {"noseenotick", "itshallnottick", "does_potato_tick"}, applyIfPresent = false)
public abstract class WorldMixin {
    @ModifyReturnValue(method = "shouldUpdatePostDeath", at = @At("RETURN"))
    private boolean shouldUpdatePostDeath(boolean original, Entity entity) {
        if (original) {
            return !Neruina.getInstance().getTickHandler().isErrored(entity);
        }

        return false;
    }

    @WrapOperation(method = "tickEntity", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", remap = false))
    private void catchTickingEntities$notTheCauseOfTickLag(Consumer<Object> instance, Object entity, Operation<Void> original) {
        Neruina.getInstance().getTickHandler().safelyTickEntities(instance, (Entity) entity, original);
    }
}
