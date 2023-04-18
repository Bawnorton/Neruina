package com.bawnorton.neruina.render.overlay;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

public class Line extends Renderer {

    public Vec3d start;
    public Vec3d end;
    public Colour colour;

    public Line(Vec3d start, Vec3d end, Colour colour) {
        this.start = start;
        this.end = end;
        this.colour = colour;
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumer vertexConsumer, Vec3d cameraPos) {
        this.putVertex(vertexConsumer, matrixStack, this.start, cameraPos);
        this.putVertex(vertexConsumer, matrixStack, this.end, cameraPos);
    }

    protected void putVertex(VertexConsumer vertexConsumer, MatrixStack matrixStack, Vec3d pos, Vec3d cameraPos) {
        vertexConsumer.vertex(
                matrixStack.peek().getPositionMatrix(),
                (float) (pos.x - cameraPos.x),
                (float) (pos.y - cameraPos.y),
                (float) (pos.z - cameraPos.z)
        ).color(
                this.colour.getFRed(),
                this.colour.getFGreen(),
                this.colour.getFBlue(),
                1f
        ).next();
    }
}