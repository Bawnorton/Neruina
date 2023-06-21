package com.bawnorton.neruina_test.block.entity;

import com.bawnorton.neruina_test.NeruinaTest;
import com.bawnorton.neruina_test.NeruinaTestExpectPlatform;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class CrashingBlockEntity extends BlockEntity {
    public CrashingBlockEntity(BlockPos pos, BlockState state) {
        super(NeruinaTestExpectPlatform.getCrashingBlockEntity(), pos, state);
    }
}
