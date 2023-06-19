package com.bawnorton.neruina.mixin;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.networking.Networking;
import com.bawnorton.neruina.thread.ConditionalRunnable;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin {

    @Inject(method = "removeBlockEntity", at = @At("HEAD"))
    private void removeErrored(BlockPos pos, CallbackInfo ci) {
        BlockEntity blockEntity = ((WorldChunk) (Object) this).getBlockEntity(pos);
        if (blockEntity != null && Neruina.isErrored(blockEntity)) {
            Neruina.removeErrored(blockEntity);
        }
    }

    @SuppressWarnings("unused")
    @Mixin(WorldChunk.DirectBlockEntityTickInvoker.class)
    private abstract static class DirectBlockEntityTickInvokerMixin {

        @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntityTicker;tick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/BlockEntity;)V"))
        private void catchTickingBlockEntity(BlockEntityTicker<? extends BlockEntity> instance, World world, BlockPos pos, BlockState state, BlockEntity blockEntity, Operation<Void> original) {
            try {
                if (Neruina.isErrored(blockEntity)) {
                    if(world.isClient) return;

                    WorldChunk chunk = world.getWorldChunk(pos);
                    chunk.removeBlockEntityTicker(pos);
                    return;
                }
                original.call(instance, world, pos, state, blockEntity);
            } catch (Throwable e) {
                String message = String.format("§b[Neruina]: §cCaught Ticking Block Entity [%s] at position [x=%s, y=%s, z=%s]. Please resolve.", state.getBlock().getName().getString(), pos.getX(), pos.getY(), pos.getZ());
                Neruina.LOGGER.warn((world.isClient? "Client: " : "Server: ") + message, e);
                Neruina.addErrored(blockEntity);
                if (world instanceof ServerWorld serverWorld) {
                    PlayerManager playerManager = serverWorld.getServer().getPlayerManager();
                    playerManager.getPlayerList().forEach(player -> Networking.sendBadBlockPacket(player, pos));
                    ConditionalRunnable.create(() -> playerManager.broadcast(Text.of(message), false), () -> playerManager.getCurrentPlayerCount() > 0);
                }
            }
        }
    }
}
