package com.bawnorton.neruina_test;

import com.bawnorton.neruina_test.block.entity.CrashingBlockEntity;
import com.bawnorton.neruina_test.entity.CrashingEntity;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;

public class NeruinaTestExpectPlatform {

    @ExpectPlatform
    public static EntityType<CrashingEntity> getCrashingEntity() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static BlockEntityType<CrashingBlockEntity> getCrashingBlockEntity() {
        throw new AssertionError();
    }
}
