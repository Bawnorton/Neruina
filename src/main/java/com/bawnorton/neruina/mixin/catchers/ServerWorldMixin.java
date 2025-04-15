package com.bawnorton.neruina.mixin.catchers;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.handler.TickHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    //? if >1.21.4 {
    /*@Inject(method = "onBlockStateChanged", at = @At("HEAD"))
    *///?} else {
    @Inject(method = "onBlockChanged", at = @At("HEAD"))
    //?}
    private void removeErrored(BlockPos pos, BlockState oldBlock, BlockState newBlock, CallbackInfo ci) {
        TickHandler tickHandler = Neruina.getInstance().getTickHandler();
        if (tickHandler.isErrored(oldBlock, pos)) {
            tickHandler.removeErrored(oldBlock, pos);
        }
    }

    @WrapOperation(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;randomTick(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)V"))
    private void catchTickingBlockState$notTheCauseOfTickLag(BlockState instance, ServerWorld world, BlockPos pos, @Coerce Object random, Operation<Void> original) {
        Neruina.getInstance().getTickHandler().safelyTickBlockState(instance, world, pos, random, original);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickNeruinaHandler(CallbackInfo ci) {
        Neruina.getInstance().getTickHandler().tick();
    }
}