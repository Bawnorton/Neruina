package com.bawnorton.neruina.mixin;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.config.Config;
import com.bawnorton.neruina.handler.MessageHandler;
import com.bawnorton.neruina.handler.TickHandler;
import com.llamalad7.mixinextras.sugar.Local;
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@MixinEnvironment
@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Inject(
            method = "placeNewPlayer",
            at = @At("TAIL")
    )
    private void sendSuspendedInfoOnJoin(CallbackInfo ci, @Local(argsOnly = true) ServerPlayer player) {
        TickHandler tickHandler = Neruina.getInstance().getTickHandler();
        int count = tickHandler.getTickingEntries().size();
        if (count > 0) {
            MessageHandler messageHandler = Neruina.getInstance().getMessageHandler();
            Component message = messageHandler.generateSuspendedInfo(player);
            int permissionLevel = Config.minPermissionLevelForMessages;
            if(permissionLevel < 0) return;

            if(player.hasPermissions(permissionLevel)) {
                player.sendSystemMessage(message, false);
            }
        }
    }
}
