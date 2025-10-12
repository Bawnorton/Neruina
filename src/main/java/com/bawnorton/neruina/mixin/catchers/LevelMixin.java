package com.bawnorton.neruina.mixin.catchers;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.util.annotation.ConditionalMixin;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@MixinEnvironment
@Mixin(value = Level.class, priority = 1500)
@ConditionalMixin(modids = {"noseenotick", "does_potato_tick"}, applyIfPresent = false)
public abstract class LevelMixin {
	@ModifyReturnValue(method = "shouldTickDeath", at = @At("RETURN"))
	private boolean dontTickIfErrored(boolean original, Entity entity) {
		if (original) {
			return !Neruina.getInstance().getTickHandler().isErrored(entity);
		}

		return false;
	}

	@WrapOperation(method = "guardEntityTick", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", remap = false))
	private void catchTickingEntities$notTheCauseOfTickLag(Consumer<Object> instance, Object entity, Operation<Void> original) {
		Neruina.getInstance().getTickHandler().safelyTickEntities(instance, (Entity) entity, original);
	}
}
