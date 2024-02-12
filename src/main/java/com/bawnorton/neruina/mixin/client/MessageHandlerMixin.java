package com.bawnorton.neruina.mixin.client;

import com.bawnorton.neruina.mixin.accessor.MutableTextAccessor;
import com.bawnorton.neruina.platform.Platform;
import com.bawnorton.neruina.version.VersionedText;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.ArrayList;
import java.util.List;


/*? if >=1.19 {*/
import net.minecraft.client.network.message.MessageHandler;

@Mixin(MessageHandler.class)
/*?} else {*//*
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
            if(!(text instanceof MutableText mutableText)) return text;

            Text content = mutableText.getSiblings().get(1);
            if(!(content instanceof MutableText contentMutableText)) return text;

            Pair<MutableText, MutableText> deepestParentSiblingPair = neruina$traverseSiblings(contentMutableText);
            if(deepestParentSiblingPair == null) return text;

            MutableText deepestSibling = deepestParentSiblingPair.getSecond();
            deepestSibling = (MutableText) VersionedText.concatDelimited(
                    VersionedText.SPACE,
                    deepestSibling,
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
            List<Text> siblings = deepestParentSiblingPair.getFirst().getSiblings();
            /*? if >=1.20 {*//*
            siblings = new ArrayList<>(siblings);
            siblings.set(siblings.size() - 1, deepestSibling);
            ((MutableTextAccessor) deepestParentSiblingPair.getFirst()).setSiblings(siblings);
            *//*?} else {*/
            siblings.set(siblings.size() - 1, deepestSibling);
            /*?}*/
            return text;
        }
        return text;
    }

    @Unique
    private Pair<MutableText, MutableText> neruina$traverseSiblings(MutableText contentMutableText) {
        Pair<MutableText, MutableText> parentSiblingPair = Pair.of(contentMutableText, contentMutableText);
        MutableText deepestSibling = parentSiblingPair.getSecond();
        while (!deepestSibling.getSiblings().isEmpty()) {
            Text next = deepestSibling.getSiblings().get(deepestSibling.getSiblings().size() - 1);
            if (next instanceof MutableText nextMutableText) {
                parentSiblingPair = Pair.of(deepestSibling, nextMutableText);
                deepestSibling = nextMutableText;
            } else {
                return null;
            }
        }
        return parentSiblingPair;
    }
}
