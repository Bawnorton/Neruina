//? if <=1.21.1 {
/*package com.bawnorton.neruina.mixin.compat.noseenotick;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.util.annotation.ConditionalMixin;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import java.util.function.Consumer;

@Mixin(value = Level.class, priority = 1500)
@ConditionalMixin(modids = "noseenotick")
public abstract class LevelMixin {
    @ModifyReturnValue(method = "shouldTickDeath", at = @At("RETURN"))
    private boolean dontTickIfErrored(boolean original, Entity entity) {
        if (original) {
            return !Neruina.getInstance().getTickHandler().isErrored(entity);
        }

        return false;
    }

    @SuppressWarnings({"MixinAnnotationTarget", "InvalidInjectorMethodSignature"})
    @WrapOperation(
            method = "guardEntityTick",
            at = {
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/entity/TickOptimizer;entityTicking(Ljava/util/function/Consumer;Lnet/minecraft/entity/Entity;Lnet/minecraft/world/Level;Lnet/minecraft/util/math/random/Random;)V",
                            remap = false
                    ),
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/entity/TickOptimizer;entityTicking(Ljava/util/function/Consumer;Lnet/minecraft/class_1297;Lnet/minecraft/class_1937;Lnet/minecraft/class_8892;)V",
                            remap = false
                    )
            },
            require = 0
    )
    private void catchTickingEntities(Consumer<Entity> consumer, Entity entity, Level level, @Coerce Object random, Operation<Void> original) {
        Neruina.getInstance().getTickHandler().safelyTickEntities(consumer, entity, level, random, original);
    }
}
*///?}