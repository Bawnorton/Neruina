package com.bawnorton.neruina.mixin.v118;

import com.bawnorton.neruina.annotation.VersionedMixin;
import com.bawnorton.neruina.handler.NeruinaTickHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@SuppressWarnings("unused")
@Mixin(ServerWorld.class)
@VersionedMixin("=1.18.2")
public abstract class ServerWorldMixin {
    @Inject(method = "onBlockChanged", at = @At("HEAD"))
    private void removeErrored(BlockPos pos, BlockState oldBlock, BlockState newBlock, CallbackInfo ci) {
        if (NeruinaTickHandler.isErrored(pos, oldBlock)) {
            NeruinaTickHandler.removeErrored(pos, oldBlock);
        }
    }

    @WrapOperation(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;randomTick(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V"))
    private void catchTickingBlockState(BlockState instance, ServerWorld world, BlockPos pos, Random random, Operation<Void> original) {
        NeruinaTickHandler.safelyTickBlockState$notTheCauseOfTickLag(instance, world, pos, random, original);
    }
}
