package com.bawnorton.neruina.mixin;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.thread.ConditionalRunnable;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.message.MessageType;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@SuppressWarnings("unused")
@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;playerTick()V"))
    private void tick(ServerPlayerEntity instance, Operation<Void> original) {
        try {
            original.call(instance);
        } catch (Throwable e) {
            String message = Text.translatable("neruina.ticking.player", instance.getName().getString()).getString();
            Neruina.LOGGER.warn(message, e);
            Text text = Text.of(message);
            if (instance.getWorld() != null) {
                PlayerManager playerManager = instance.getWorld().getServer().getPlayerManager();
                ConditionalRunnable.create(() -> playerManager.broadcast(Text.of(message), MessageType.SYSTEM), () -> playerManager.getCurrentPlayerCount() > 0);
            }
            instance.networkHandler.disconnect(Text.of(Text.translatable("neruina.kick.message").getString()));
        }
    }
}
