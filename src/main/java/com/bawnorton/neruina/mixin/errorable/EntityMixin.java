package com.bawnorton.neruina.mixin.errorable;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.extend.Errorable;
import com.bawnorton.neruina.util.TickingEntry;
import com.bawnorton.neruina.version.VersionedText;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.UUID;

@Mixin(Entity.class)
public abstract class EntityMixin implements Errorable {
    @Shadow public abstract Text getName();

    @Shadow public abstract World getWorld();

    @Unique
    private boolean neruina$errored = false;

    @Unique
    private UUID neruina$tickingEntryId = null;

    @Override
    public boolean neruina$isErrored() {
        return neruina$errored;
    }

    @Override
    public void neruina$setErrored() {
        neruina$errored = true;
    }

    @Override
    public void neruina$clearErrored() {
        neruina$errored = false;
    }

    @Override
    public void neruina$setTickingEntry(UUID uuid) {
        neruina$tickingEntryId = uuid;
    }

    @Override
    public UUID neruina$getTickingEntry() {
        return neruina$tickingEntryId;
    }

    @ModifyReturnValue(method = "writeNbt", at = @At("RETURN"))
    private NbtCompound writeErroredToNbt(NbtCompound original) {
        if (neruina$errored) {
            original.putBoolean("neruina$errored", true);
        }
        if (neruina$tickingEntryId != null) {
            original.putUuid("neruina$tickingEntryId", neruina$tickingEntryId);
        }
        return original;
    }

    @Inject(method = "readNbt", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;onGround:Z", opcode = Opcodes.PUTFIELD))
    private void readErroredFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("neruina$errored")) {
            neruina$errored = nbt.getBoolean("neruina$errored");
        }
        if (nbt.contains("neruina$tickingEntryId")) {
            neruina$tickingEntryId = nbt.getUuid("neruina$tickingEntryId");
        }
    }

    @ModifyReturnValue(method = "isInvulnerableTo", at = @At("RETURN"))
    private boolean ignoreDamageWhenErrored(boolean original, DamageSource source) {
        if (original) return true;

        if (neruina$errored && neruina$tickingEntryId != null) {
            if (source.getAttacker() instanceof ServerPlayerEntity player) {
                TickingEntry entry = Neruina.TICK_HANDLER.getTickingEntry(neruina$tickingEntryId);
                Neruina.MESSAGE_HANDLER.sendToPlayer(
                        player,
                        VersionedText.translatable("neruina.suspended.entity", getName().getString()),
                        Neruina.MESSAGE_HANDLER.generateEntityActions((Entity) (Object) this),
                        Neruina.MESSAGE_HANDLER.generateResourceActions(entry)
                );
            }
            /*? if >=1.20 { */
            return source != getWorld().getDamageSources().genericKill();
            /*? } elif >=1.19 { *//*
            return source != getWorld().getDamageSources().outOfWorld();
            *//*? } else { *//*
            return source != net.minecraft.entity.damage.DamageSource.OUT_OF_WORLD;
            *//*? }*/
        }
        return false;
    }
}
