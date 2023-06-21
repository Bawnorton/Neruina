package com.bawnorton.neruina_test.client.render.model;

import com.bawnorton.neruina_test.entity.CrashingEntity;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class CrashingEntityModel extends BipedEntityModel<CrashingEntity> {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(new Identifier("minecraft", "textures/entity/player/wide/steve"), "main");

    public CrashingEntityModel(ModelPart root) {
        super(root);
    }

    public static TexturedModelData getTextureModelData() {
        ModelData meshdefinition = getModelData(new Dilation(0), 0);
        return TexturedModelData.of(meshdefinition, 64, 64);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        super.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
}
