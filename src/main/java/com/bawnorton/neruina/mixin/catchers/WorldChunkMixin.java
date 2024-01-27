package com.bawnorton.neruina.mixin.catchers;

import com.bawnorton.neruina.Neruina;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin {
    @Shadow
    public abstract @Nullable BlockEntity getBlockEntity(BlockPos pos);

    @Inject(method = "removeBlockEntity", at = @At("HEAD"))
    private void removeErrored(BlockPos pos, CallbackInfo ci) {
        BlockEntity blockEntity = getBlockEntity(pos);
        if (Neruina.TICK_HANDLER.isErrored(blockEntity)) {
            Neruina.TICK_HANDLER.removeErrored(blockEntity);
        }
    }

    @Mixin(WorldChunk.DirectBlockEntityTickInvoker.class)
    private abstract static class DirectBlockEntityTickInvokerMixin {
        @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntityTicker;tick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/BlockEntity;)V"))
        private void catchTickingBlockEntity$notTheCauseOfTickLag(BlockEntityTicker<? extends BlockEntity> instance, World world, BlockPos pos, BlockState state, BlockEntity blockEntity, Operation<Void> original) {
            Neruina.TICK_HANDLER.safelyTickBlockEntity(instance, world, pos, state, blockEntity, original);
        }
    }
}
