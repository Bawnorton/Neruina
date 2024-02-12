package com.bawnorton.neruina.mixin.accessor;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WorldChunk.class)
public interface WorldChunkAccessor {
    @Invoker
    void invokeRemoveBlockEntityTicker(BlockPos pos);
}
