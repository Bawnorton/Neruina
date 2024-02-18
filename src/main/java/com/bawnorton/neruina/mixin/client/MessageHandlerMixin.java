package com.bawnorton.neruina.mixin.client;

import com.bawnorton.neruina.mixin.accessor.MutableTextAccessor;
import com.bawnorton.neruina.platform.Platform;
import com.bawnorton.neruina.version.VersionedText;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/*? if >=1.19 {*/

@Mixin(MessageHandler.class)
/*?} else {*//*
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.network.ClientPlayNetworkHandler;

@Mixin(ClientPlayNetworkHandler.class)
*//*?}*/
public abstract class MessageHandlerMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    /*? if >=1.19 {*/
    @ModifyVariable(method = "onGameMessage", at = @At("HEAD"), argsOnly = true)
    /*?} else {*//*
    @ModifyExpressionValue(method = "onGameMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/GameMessageS2CPacket;getMessage()Lnet/minecraft/text/Text;"))
    *//*?}*/
    private Text appendOpenLogAction(Text text) {
        if (client.getServer() == null) {
            return text;
        }

        if (text.toString().contains("neruina.copy_crash")) {
            neruina$findCopyCrashAction(text).ifPresent(parentActionPair -> {
                MutableText parent = parentActionPair.getFirst();
                MutableText action = parentActionPair.getSecond();

                List<Text> siblings = new ArrayList<>(parent.getSiblings());
                int index = -1;
                String actionContent = action.getString();
                for (int i = 0; i < siblings.size(); i++) {
                    String siblingContent = siblings.get(i).getString();
                    if (siblingContent.contains(actionContent)) {
                        index = i;
                        break;
                    }
                }
                if(index == -1) {
                    return;
                }

                Text openLog = Texts.bracketed(
                        VersionedText.withStyle(
                                VersionedText.translatable("neruina.open_log"),
                                style -> style.withColor(Formatting.LIGHT_PURPLE)
                                        .withClickEvent(new ClickEvent(
                                                ClickEvent.Action.OPEN_FILE,
                                                Platform.getLogPath().toString()
                                        ))
                                        .withHoverEvent(new HoverEvent(
                                                HoverEvent.Action.SHOW_TEXT,
                                                VersionedText.translatable("neruina.open_log.tooltip")
                                        ))
                        )
                );

                siblings.set(index, VersionedText.concatDelimited(VersionedText.SPACE, siblings.get(index), openLog));
                ((MutableTextAccessor) parent).setSiblings(siblings);
            });
        }
        return text;
    }

    @Unique
    private Optional<Pair<MutableText, MutableText>> neruina$findCopyCrashAction(Text text) {
        return neruina$findCopyCrashAction(null, text);
    }

    @Unique
    private Optional<Pair<MutableText, MutableText>> neruina$findCopyCrashAction(MutableText parent, Text text) {
        if(!(text instanceof MutableText mutableText)) {
            return Optional.empty();
        }

        TextContent textContent = mutableText.getContent();
        if(textContent instanceof TranslatableTextContent translatableTextContent) {
            String key = translatableTextContent.getKey();
            if(key.equals("neruina.copy_crash")) {
                return Optional.of(Pair.of(parent, mutableText));
            }

            for(Object arg : translatableTextContent.getArgs()) {
                if (!(arg instanceof Text textArg)) {
                    continue;
                }

                Optional<Pair<MutableText, MutableText>> action = neruina$findCopyCrashAction(parent, textArg);
                if(action.isPresent()) {
                    return action;
                }
            }
        } else {
            String content = textContent.visit(Optional::ofNullable).orElse("");
            if(content.equals("neruina.copy_crash")) {
                return Optional.of(Pair.of(parent, mutableText));
            }
        }


        for(Text sibling : mutableText.getSiblings()) {
            Optional<Pair<MutableText, MutableText>> action = neruina$findCopyCrashAction(mutableText, sibling);
            if(action.isPresent()) {
                return action;
            }
        }
        return Optional.empty();
    }
}
