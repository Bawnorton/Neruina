package com.bawnorton.neruina.mixin;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.thread.ConditionalRunnable;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.BlockState;
import net.minecraft.network.message.MessageType;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    @Inject(method = "onBlockChanged", at = @At("HEAD"))
    private void removeErrored(BlockPos pos, BlockState oldBlock, BlockState newBlock, CallbackInfo ci) {
        if (Neruina.isErrored(pos, oldBlock)) {
            Neruina.removeErrored(pos, oldBlock);
        }
    }

    @WrapOperation(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;randomTick(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)V"))
    private void catchTickingBlockState(BlockState instance, ServerWorld world, BlockPos pos, Random random, Operation<Void> original) {
        try {
            if (Neruina.isErrored(pos, instance)) {
                return;
            }
            original.call(instance, world, pos, random);
        } catch (Throwable e) {
            String message = Text.translatable("neruina.ticking.block_state", instance.getBlock().getName(), pos.getX(), pos.getY(), pos.getZ()).getString();
            Neruina.LOGGER.warn("Server: " + message, e);
            Neruina.addErrored(pos, instance);
            PlayerManager playerManager = world.getServer().getPlayerManager();
            ConditionalRunnable.create(() -> playerManager.broadcast(Text.of(message), MessageType.SYSTEM), () -> playerManager.getCurrentPlayerCount() > 0);
        }
    }
}
