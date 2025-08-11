package com.bawnorton.neruina.mixin.catchers;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.handler.TickHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@MixinEnvironment
@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {
    @Shadow
    public abstract @Nullable BlockEntity getBlockEntity(BlockPos pos);

    @Inject(method = "removeBlockEntity", at = @At("HEAD"))
    private void removeErrored(BlockPos pos, CallbackInfo ci) {
        BlockEntity blockEntity = getBlockEntity(pos);
        TickHandler tickHandler = Neruina.getInstance().getTickHandler();
        if (tickHandler.isErrored(blockEntity)) {
            tickHandler.removeErrored(blockEntity);
        }
    }

    @MixinEnvironment
    @Mixin(targets = "net.minecraft.world.level.chunk.LevelChunk$BoundTickingBlockEntity")
    private abstract static class BoundTickingBlockEntityMixin {
        @WrapOperation(
                method = "tick",
                at = @At(
                        value = "INVOKE",
                        target = "Lnet/minecraft/world/level/block/entity/BlockEntityTicker;tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntity;)V"
                )
        )
        private void catchTickingBlockEntity$notTheCauseOfTickLag(BlockEntityTicker<? extends BlockEntity> instance, Level level, BlockPos pos, BlockState state, BlockEntity blockEntity, Operation<Void> original) {
            Neruina.getInstance().getTickHandler().safelyTickBlockEntity(instance, level, pos, state, blockEntity, original);
        }
    }
}
