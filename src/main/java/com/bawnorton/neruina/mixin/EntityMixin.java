package com.bawnorton.neruina.mixin;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.extend.Errorable;
import com.bawnorton.neruina.handler.NeruinaTickHandler;
import com.bawnorton.neruina.version.VersionedText;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import java.util.UUID;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public abstract World getWorld();

    @Shadow
    public abstract Text getName();

    @Shadow public abstract UUID getUuid();

    @ModifyReturnValue(method = "isInvulnerableTo", at = @At("RETURN"))
    private boolean ignoreDamageWhenErrored(boolean original, DamageSource source) {
        if (original) {
            return true;
        }

        if (((Errorable) this).neruina$isErrored()) {
            if (source.getAttacker() instanceof ServerPlayerEntity player) {
                NeruinaTickHandler.TickingEntry entry = Neruina.TICK_HANDLER.getTickingEntry(getUuid());
                Neruina.MESSAGE_HANDLER.sendToPlayer(
                        player,
                        VersionedText.translatable("neruina.suspended.entity", getName().getString()),
                        Neruina.MESSAGE_HANDLER.generateEntityActions((Entity) (Object) this),
                        Neruina.MESSAGE_HANDLER.generateResourceActions(entry.e())
                );
            }
            /*? if >=1.20 { */
            return source != getWorld().getDamageSources().genericKill();
            /*? } else if >=1.19 { *//*/*
            return source != getWorld().getDamageSources().outOfWorld();
            *//*? } else { *//*
            return source != net.minecraft.entity.damage.DamageSource.OUT_OF_WORLD;
            *//*? }*/
        }
        return false;
    }
}
