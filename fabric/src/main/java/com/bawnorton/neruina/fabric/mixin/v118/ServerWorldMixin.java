package com.bawnorton.neruina.fabric.mixin.v118;

import com.bawnorton.neruina.annotation.VersionedMixin;
import com.bawnorton.neruina.handler.NeruinaTickHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Random;

@Mixin(ServerWorld.class)
@VersionedMixin("=1.18.2")
public abstract class ServerWorldMixin {
    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference", "InvalidInjectorMethodSignature"})
    @WrapOperation(method = "tickChunk", at = @At(value = "INVOKE", target = "net/minecraft/class_2680.method_26199(Lnet/minecraft/class_3218;Lnet/minecraft/class_2338;Ljava/util/Random;)V", remap = false))
    private void catchTickingBlockState(BlockState instance, ServerWorld world, BlockPos pos, Random random, Operation<Void> original) {
        NeruinaTickHandler.safelyTickBlockState$notTheCauseOfTickLag(instance, world, pos, random, original);
    }
}
