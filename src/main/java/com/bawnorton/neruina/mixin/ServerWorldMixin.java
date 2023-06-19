package com.bawnorton.neruina.mixin;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.networking.Networking;
import com.bawnorton.neruina.thread.ConditionalRunnable;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.BlockState;
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
            String message = String.format("§b[Neruina]: §cCaught Ticking BlockState from random tick [%s] at position [x=%s, y=%s, z=%s]. Please resolve.", instance.getBlock().getName().getString(), pos.getX(), pos.getY(), pos.getZ());
            Neruina.LOGGER.warn("Server: " + message, e);
            Neruina.addErrored(pos, instance);
            PlayerManager playerManager = world.getServer().getPlayerManager();
            playerManager.getPlayerList().forEach(player -> Networking.sendBadBlockPacket(player, pos));
            ConditionalRunnable.create(() -> playerManager.broadcast(Text.of(message), false), () -> playerManager.getCurrentPlayerCount() > 0);
        }
    }
}
