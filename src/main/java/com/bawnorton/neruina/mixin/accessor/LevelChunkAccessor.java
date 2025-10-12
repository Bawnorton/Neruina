package com.bawnorton.neruina.mixin.accessor;

import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@MixinEnvironment
@Mixin(LevelChunk.class)
public interface LevelChunkAccessor {
	@Invoker("removeBlockEntityTicker")
	void neruina$removeBlockEntityTicker(BlockPos pos);
}
