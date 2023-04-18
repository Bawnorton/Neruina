package com.bawnorton.neruina.mixin;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.thread.ConditionalRunnable;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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
        } catch (RuntimeException e) {
            String message = String.format("§b[Neruina]: §cCaught Ticking Player, %s has been kicked.", instance.getName().getString());
            Neruina.LOGGER.warn(message, e);
            Text text = Text.of(message);
            if (instance.world instanceof ServerWorld serverWorld) {
                PlayerManager playerManager = serverWorld.getServer().getPlayerManager();
                ConditionalRunnable.create(() -> playerManager.getPlayerList().forEach(player -> player.sendMessage(Text.of(message), false)), () -> playerManager.getCurrentPlayerCount() >= 1);
            }
            instance.networkHandler.disconnect(Text.of("§b[Neruina]: §cYou have been kicked due to a ticking exception on the player. Please remove the culprit mod or contact an admin."));
        }
    }
}
