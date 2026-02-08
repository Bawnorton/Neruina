package com.bawnorton.neruina.mixin.accessor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LevelChunk.class)
public interface LevelChunkAccessor {
	@Invoker("removeBlockEntityTicker")
	void neruina$removeBlockEntityTicker(BlockPos pos);
}
