package com.bawnorton.neruina.mixin;

import com.bawnorton.neruina.extend.Errorable;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public abstract World getWorld();

    @ModifyReturnValue(method = "isInvulnerableTo", at = @At("RETURN"))
    private boolean ignoreDamageWhenErrored(boolean original, DamageSource source) {
        if (original) return true;

        /*? if >=1.20 { */
        return ((Errorable) this).neruina$isErrored() && source != getWorld().getDamageSources().genericKill();
        /*? } else if >=1.19 { *//*
        return ((Errorable) this).neruina$isErrored() && source != getWorld().getDamageSources().outOfWorld();
        /*? } else { *//*
        return ((Errorable) this).neruina$isErrored() && source != DamageSource.OUT_OF_WORLD;
        *//*? }*/
    }
}
