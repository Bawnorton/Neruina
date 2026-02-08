package com.bawnorton.neruina.mixin.catchers;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.handler.TickHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
	//? if <=1.21.1 {
    /*@Inject(method = "sendBlockUpdated", at = @At("HEAD"))
    private void removeErrored(BlockPos pos, BlockState oldState, BlockState newState, int flags, CallbackInfo ci) {
      TickHandler tickHandler = Neruina.getInstance().getTickHandler();
      if (tickHandler.isErrored(oldState, pos)) {
        tickHandler.removeErrored(oldState, pos);
      }
    }
    *///?} else {
	@Inject(method = "updatePOIOnBlockStateChange", at = @At("HEAD"))
	private void removeErrored(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
		TickHandler tickHandler = Neruina.getInstance().getTickHandler();
		if (tickHandler.isErrored(oldState, pos)) {
			tickHandler.removeErrored(oldState, pos);
		}
	}
	//?}

	@WrapOperation(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;randomTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V"))
	private void catchTickingBlockState$notTheCauseOfTickLag(BlockState instance, ServerLevel world, BlockPos pos, @Coerce Object random, Operation<Void> original) {
		Neruina.getInstance().getTickHandler().safelyTickBlockState(instance, world, pos, random, original);
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void tickNeruinaHandler(CallbackInfo ci) {
		Neruina.getInstance().getTickHandler().tick();
	}
}