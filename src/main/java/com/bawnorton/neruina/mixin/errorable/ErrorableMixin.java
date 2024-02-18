package com.bawnorton.neruina.mixin.errorable;

import com.bawnorton.neruina.extend.Errorable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({ItemStack.class, BlockEntity.class, Entity.class})
public abstract class ErrorableMixin implements Errorable {
    @Unique
    private boolean neruina$errored = false;

    @Override
    public boolean neruina$isErrored() {
        return neruina$errored;
    }

    @Override
    public void neruina$setErrored() {
        neruina$errored = true;
    }

    @Override
    public void neruina$clearErrored() {
        neruina$errored = false;
    }
}
