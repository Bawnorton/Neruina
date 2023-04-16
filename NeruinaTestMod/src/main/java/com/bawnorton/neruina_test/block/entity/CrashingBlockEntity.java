package com.bawnorton.neruina_test.block.entity;

import com.bawnorton.neruina_test.NeruinaTestRegistrar;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class CrashingBlockEntity extends BlockEntity {
    public CrashingBlockEntity(BlockPos pos, BlockState state) {
        super(NeruinaTestRegistrar.CRASHING_BLOCK_ENTITY, pos, state);
    }
}
