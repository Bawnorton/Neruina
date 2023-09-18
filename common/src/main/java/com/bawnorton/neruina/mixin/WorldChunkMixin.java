package com.bawnorton.neruina.mixin;

import com.bawnorton.neruina.handler.NeruinaTickHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin {
    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "removeBlockEntity", at = @At("HEAD"))
    private void removeErrored(BlockPos pos, CallbackInfo ci) {
        BlockEntity blockEntity = ((WorldChunk) (Object) this).getBlockEntity(pos);
        if (blockEntity != null && NeruinaTickHandler.isErrored(blockEntity)) {
            NeruinaTickHandler.removeErrored(blockEntity);
        }
    }

    @SuppressWarnings("unused")
    @Mixin(WorldChunk.DirectBlockEntityTickInvoker.class)
    private abstract static class DirectBlockEntityTickInvokerMixin {
        @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntityTicker;tick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/BlockEntity;)V"))
        private void catchTickingBlockEntity(BlockEntityTicker<? extends BlockEntity> instance, World world, BlockPos pos, BlockState state, BlockEntity blockEntity, Operation<Void> original) {
            NeruinaTickHandler.safelyTickBlockEntity$notTheCauseOfTickLag(instance, world, pos, state, blockEntity, original);
        }
    }
}
