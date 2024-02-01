package com.bawnorton.neruina.mixin.client;

import com.bawnorton.neruina.platform.Platform;
import com.bawnorton.neruina.version.VersionedText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;


/*? if >=1.19 {*/
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import net.minecraft.client.network.message.MessageHandler;

@Mixin(MessageHandler.class)
/*?} else {*//*
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.network.ClientPlayNetworkHandler;

@Mixin(ClientPlayNetworkHandler.class)
*//*?}*/
public abstract class MessageHandlerMixin {
    @Shadow @Final private MinecraftClient client;

    /*? if >=1.19 {*/
    @ModifyVariable(method = "onGameMessage", at = @At("HEAD"), argsOnly = true)
    /*?} else {*//*
    @ModifyExpressionValue(method = "onGameMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/GameMessageS2CPacket;getMessage()Lnet/minecraft/text/Text;"))
    *//*?}*/
    private Text appendOpenLogAction(Text text) {
        if(client.getServer() == null) return text;

        if (text.toString().contains("neruina.info")) {
            text = VersionedText.concatDelimited(
                    VersionedText.SPACE,
                    text,
                    Texts.bracketed(
                            VersionedText.withStyle(
                                    VersionedText.translatableWithFallback("neruina.open_log", "Open Log"),
                                    style -> style.withColor(Formatting.LIGHT_PURPLE)
                                                  .withClickEvent(new ClickEvent(
                                                          ClickEvent.Action.OPEN_FILE,
                                                          Platform.getLogPath().toString()
                                                  ))
                                                  .withHoverEvent(new HoverEvent(
                                                          HoverEvent.Action.SHOW_TEXT,
                                                          VersionedText.translatableWithFallback(
                                                                  "neruina.open_log.tooltip",
                                                                  "Opens your latest.log file"
                                                          )
                                                  ))
                            )
                    )
            );
        }
        return text;
    }
}
