package com.bawnorton.neruina.extend;

import net.minecraft.util.math.BlockPos;

public interface ErrorableBlockState {
    boolean neruina$isErrored(BlockPos pos);
    void neruina$setErrored(BlockPos pos);
    void neruina$clearErrored(BlockPos pos);
}
