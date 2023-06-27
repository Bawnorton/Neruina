package com.bawnorton.neruina.mixin;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.thread.ConditionalRunnable;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(value = World.class, priority = 1500)
public abstract class WorldMixin {
    @Inject(method = "shouldUpdatePostDeath", at = @At("HEAD"), cancellable = true)
    public void shouldUpdatePostDeath(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if(Neruina.isErrored(entity)) {
            cir.setReturnValue(false);
        }
    }

    @WrapOperation(method = "tickEntity", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"))
    public <T extends Entity> void catchTickingEntities(Consumer<T> instance, Object param, Operation<Void> original) {
        Entity entity = (Entity) param;
        try {
            if(Neruina.isErrored(entity)) {
                if(entity instanceof PlayerEntity) return;
                if(entity.getWorld().isClient) return;

                entity.kill();
                entity.remove(Entity.RemovalReason.KILLED);
                entity.baseTick();
                Neruina.removeErrored(entity);
                return;
            }
            original.call(instance, param);
        } catch (Throwable e) {
            BlockPos pos = entity.getBlockPos();
            String message = new TranslatableText("neruina.ticking.entity", entity.getName().getString(), pos.getX(), pos.getY(), pos.getZ()).getString();
            Neruina.LOGGER.warn((entity.getWorld().isClient? "Client: " : "Server: ") + message, e);
            Neruina.addErrored(entity);
            if (entity.getWorld() instanceof ServerWorld serverWorld) {
                PlayerManager playerManager = serverWorld.getServer().getPlayerManager();
                ConditionalRunnable.create(() -> playerManager.getPlayerList().forEach(player -> player.sendMessage(Text.of(message), false)), () -> playerManager.getCurrentPlayerCount() >= 1);
            }
        }
    }
}
