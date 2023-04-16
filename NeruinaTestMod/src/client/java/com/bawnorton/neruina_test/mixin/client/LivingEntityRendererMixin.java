package com.bawnorton.neruina_test.mixin.client;

import com.bawnorton.neruina_test.NeruinaTest;
import com.bawnorton.neruina_test.entity.CrashingEntity;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin extends EntityRenderer<LivingEntity> {
	@Shadow
	protected EntityModel<LivingEntity> model;

	private MatrixStack matrixStack;
	private LivingEntity livingEntity;

	protected LivingEntityRendererMixin(EntityRendererFactory.Context ctx) {
		super(ctx);
	}

	@Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"))
	private void renderHead(LivingEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
		this.matrixStack = matrixStack;
		this.livingEntity = livingEntity;
	}

	@Redirect(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumerProvider;getBuffer(Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;"))
	private VertexConsumer getBuffer(VertexConsumerProvider vertexConsumerProvider, RenderLayer renderLayer) {
		if(livingEntity instanceof CrashingEntity) {
			return new OverlayVertexConsumer(vertexConsumerProvider.getBuffer(RenderLayer.getEntitySolid(NeruinaTest.id("textures/block/crashing_block.png"))), matrixStack.peek().getPositionMatrix(), matrixStack.peek().getNormalMatrix(), 1.0F);
		}
		return vertexConsumerProvider.getBuffer(renderLayer);
	}
}