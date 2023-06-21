package com.bawnorton.neruina_test.client;

import com.bawnorton.neruina_test.NeruinaTest;
import com.bawnorton.neruina_test.NeruinaTestExpectPlatform;
import com.bawnorton.neruina_test.client.render.CrashingEntityRenderer;
import com.bawnorton.neruina_test.client.render.model.CrashingEntityModel;
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;

public class NeruinaTestClient {
    public static void init() {
        NeruinaTest.LOGGER.info("Initializing NeruinaTestClient");
        EntityRendererRegistry.register(NeruinaTestExpectPlatform::getCrashingEntity, CrashingEntityRenderer::new);
        EntityModelLayerRegistry.register(CrashingEntityModel.LAYER_LOCATION, CrashingEntityModel::getTextureModelData);
    }
}
