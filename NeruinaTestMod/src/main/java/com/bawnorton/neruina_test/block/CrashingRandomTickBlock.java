package com.bawnorton.neruina_test.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class CrashingRandomTickBlock extends Block {
    public CrashingRandomTickBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        throw new RuntimeException("Crashing block crashed!");
    }
}
