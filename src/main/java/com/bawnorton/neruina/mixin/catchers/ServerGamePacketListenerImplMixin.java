package com.bawnorton.neruina.mixin.catchers;

import com.bawnorton.neruina.Neruina;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@MixinEnvironment
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
	@WrapOperation(
			//? if >=1.21.10 {
			method = "tickPlayer",
			//?} else {
			/*method = "tick",
			*///?}
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/level/ServerPlayer;doTick()V"
			)
	)
	private void catchTickingPlayer$notTheCauseOfTickLag(ServerPlayer instance, Operation<Void> original) {
		Neruina.getInstance().getTickHandler().safelyTickPlayer(instance, original);
	}
}