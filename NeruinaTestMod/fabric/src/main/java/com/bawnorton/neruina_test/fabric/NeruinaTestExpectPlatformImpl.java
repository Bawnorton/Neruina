package com.bawnorton.neruina_test.fabric;

import com.bawnorton.neruina_test.block.entity.CrashingBlockEntity;
import com.bawnorton.neruina_test.entity.CrashingEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;

public class NeruinaTestExpectPlatformImpl {
    public static EntityType<CrashingEntity> getCrashingEntity() {
        return NeruinaTestFabric.CRASHING_ENTITY;
    }

    public static BlockEntityType<CrashingBlockEntity> getCrashingBlockEntity() {
        return NeruinaTestFabric.CRASHING_BLOCK_ENTITY;
    }
}
