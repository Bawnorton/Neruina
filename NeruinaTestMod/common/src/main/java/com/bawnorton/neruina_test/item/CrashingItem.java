package com.bawnorton.neruina_test.item;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class CrashingItem extends Item {
    public CrashingItem(Settings settings) {
        super(settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        throw new RuntimeException("Crashing item crashed!");
    }
}
