package com.bawnorton.neruina_test;

import com.bawnorton.neruina_test.render.CrashingEntityRenderer;
import com.bawnorton.neruina_test.render.model.CrashingEntityModel;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class NeruinaTestClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(NeruinaTestRegistrar.CRASHING_ENTITY, CrashingEntityRenderer::new);
		EntityModelLayerRegistry.registerModelLayer(CrashingEntityModel.LAYER_LOCATION, CrashingEntityModel::getTextureModelData);
	}
}