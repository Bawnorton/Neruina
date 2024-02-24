package com.bawnorton.neruina.mixin.errorable;

import com.bawnorton.neruina.extend.ErrorableBlockState;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockState.class)
public abstract class BlockStateMixin implements ErrorableBlockState {
    @Unique
    private final Object2BooleanMap<BlockPos> neruina$errored = new Object2BooleanOpenHashMap<>();

    @Override
    public boolean neruina$isErrored(BlockPos pos) {
        return neruina$errored.getBoolean(pos);
    }

    @Override
    public void neruina$setErrored(BlockPos pos) {
        neruina$errored.put(pos, true);
    }

    @Override
    public void neruina$clearErrored(BlockPos pos) {
        neruina$errored.removeBoolean(pos);
    }
}
