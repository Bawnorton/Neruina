package com.bawnorton.neruina.render.overlay;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public abstract class Renderer {

    public abstract void render(MatrixStack matrixStack, VertexConsumer vertexConsumer, Vec3d cameraPos);

    protected Vec3d toVec3d(BlockPos pos) {
        return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
    }
}