package com.bawnorton.neruina_test.render;

import com.bawnorton.neruina_test.entity.CrashingEntity;
import com.bawnorton.neruina_test.render.model.CrashingEntityModel;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class CrashingEntityRenderer extends MobEntityRenderer<CrashingEntity, CrashingEntityModel> {
    public CrashingEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new CrashingEntityModel(ctx.getPart(CrashingEntityModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public Identifier getTexture(CrashingEntity entity) {
        return CrashingEntityModel.LAYER_LOCATION.getId();
    }
}
