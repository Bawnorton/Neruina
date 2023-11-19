package com.bawnorton.neruina_test.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.world.World;

public class CrashingEntity extends PathAwareEntity {
    public CrashingEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld() != null) {
            throw new RuntimeException("Crashing entity crashed!");
        }
    }
}
