package com.bawnorton.neruina.mixin;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.config.Config;
import com.bawnorton.neruina.handler.MessageHandler;
import com.bawnorton.neruina.handler.TickHandler;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Shadow @Final private MinecraftServer server;

    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    private void sendSuspendedInfoOnJoin(CallbackInfo ci, @Local(argsOnly = true) ServerPlayerEntity player) {
        TickHandler tickHandler = Neruina.getInstance().getTickHandler();
        int count = tickHandler.getTickingEntries().size();
        if (count > 0) {
            MessageHandler messageHandler = Neruina.getInstance().getMessageHandler();
            Text message = messageHandler.generateSuspendedInfo();
            //? if >1.20.7 {
            switch (MessageHandler.logLevel) {
            //?} else {
            /*switch (Config.getInstance().logLevel) {
            *///?}
                case OPERATORS -> {
                    if(player.hasPermissionLevel(server.getOpPermissionLevel())) {
                        player.sendMessage(message, false);
                    }
                }
                case EVERYONE -> player.sendMessage(message, false);
                case DISABLED -> {}
            }
        }
    }
}
