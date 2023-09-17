package com.bawnorton.neruina.mixin.forge.noseenotick;

import com.bawnorton.neruina.annotation.ConditionalMixin;
import com.bawnorton.neruina.handler.NeruinaTickHandler;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
@ConditionalMixin(modid = "noseenotick")
public abstract class WorldMixin {
    @Inject(method = "shouldUpdatePostDeath", at = @At("HEAD"), cancellable = true)
    public void shouldUpdatePostDeath(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if(NeruinaTickHandler.isErrored(entity)) {
            cir.setReturnValue(false);
        }
    }
}
