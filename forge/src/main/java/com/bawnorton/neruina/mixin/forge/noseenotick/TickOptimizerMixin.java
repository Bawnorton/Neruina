package com.bawnorton.neruina.mixin.forge.noseenotick;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.annotation.ConditionalMixin;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

import static com.bawnorton.neruina.Neruina.LOGGER;

@Pseudo
@Mixin(targets = "net.minecraft.entity.TickOptimizer", remap = false)
@ConditionalMixin(modid = "noseenotick")
public abstract class TickOptimizerMixin {
    @WrapOperation(method = "handleGuardEntityTick", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"))
    private static void catchTickingEntities(Consumer<Entity> consumer, Object param, Operation<Void> original) {
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
            original.call(consumer, param);
        } catch (Throwable e) {
            BlockPos pos = entity.getBlockPos();
            String message = new TranslatableText("neruina.ticking.entity", entity.getName().getString(), pos.getX(), pos.getY(), pos.getZ()).getString();
            LOGGER.warn((entity.getWorld().isClient? "Client: " : "Server: ") + message, e);
            Neruina.addErrored(entity);
            if (entity.getWorld() instanceof ServerWorld serverWorld) {
                PlayerManager playerManager = serverWorld.getServer().getPlayerManager();
                ConditionalRunnable.create(() -> playerManager.getPlayerList().forEach(player -> player.sendMessage(Text.of(message), false)), () -> playerManager.getCurrentPlayerCount() >= 1);
            }
        }
    }
}
