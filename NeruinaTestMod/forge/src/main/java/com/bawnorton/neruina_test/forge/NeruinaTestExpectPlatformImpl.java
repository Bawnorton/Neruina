package com.bawnorton.neruina_test.forge;

import com.bawnorton.neruina_test.block.entity.CrashingBlockEntity;
import com.bawnorton.neruina_test.entity.CrashingEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;

public class NeruinaTestExpectPlatformImpl {
    public static EntityType<CrashingEntity> getCrashingEntity() {
        return NeruinaTestForge.CRASHING_ENTITY.get();
    }

    public static BlockEntityType<CrashingBlockEntity> getCrashingBlockEntity() {
        return NeruinaTestForge.CRASHING_BLOCK_ENTITY.get();
    }
}
