package com.bawnorton.neruina.handler;

import com.bawnorton.neruina.config.Config;
import com.bawnorton.neruina.thread.ConditionalRunnable;
import com.bawnorton.neruina.util.ErroredType;
import com.bawnorton.neruina.util.TickingEntry;
import com.bawnorton.neruina.version.VersionedText;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;
import java.util.function.Consumer;

public final class MessageHandler {
    public void broadcastToPlayers(MinecraftServer server, Text message) {
        ConditionalRunnable.create(() -> {
            switch (Config.getInstance().logLevel) {
                case DISABLED -> {
                }
                case EVERYONE -> server.getPlayerManager()
                        .getPlayerList()
                        .forEach(player -> player.sendMessage(message, false));
                case OPERATORS -> server.getPlayerManager()
                        .getPlayerList()
                        .stream()
                        .filter(player -> server.getPermissionLevel(player.getGameProfile()) >= server.getOpPermissionLevel())
                        .forEach(player -> player.sendMessage(message, false));
            }
        }, () -> server.getPlayerManager().getCurrentPlayerCount() > 0);
    }

    public void broadcastToPlayers(MinecraftServer server, Text... messages) {
        broadcastToPlayers(
                server,
                VersionedText.pad(VersionedText.concatDelimited(VersionedText.LINE_BREAK, messages))
        );
    }

    public void broadcastToPlayers(MinecraftServer server, String key, Object... args) {
        broadcastToPlayers(server, formatText(key, args));
    }

    public void broadcastToPlayers(CommandContext<ServerCommandSource> context, String key, Object... args) {
        broadcastToPlayers(context.getSource().getServer(), key, args);
    }

    public Text generateEntityActions(Entity entity) {
        return VersionedText.concatDelimited(
                VersionedText.SPACE,
                generateHandlingActions(ErroredType.ENTITY, entity.getBlockPos(), entity.getUuid()),
                Texts.bracketed(VersionedText.withStyle(
                        VersionedText.translatable("neruina.kill_entity"),
                        style -> style.withColor(Formatting.DARK_RED)
                                .withClickEvent(new ClickEvent(
                                        ClickEvent.Action.RUN_COMMAND,
                                        "/neruina kill %s".formatted(entity.getUuid())
                                ))
                                .withHoverEvent(new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        VersionedText.translatable("neruina.kill_entity.tooltip")
                                ))
                ))
        );
    }

    public Text generateResourceActions(TickingEntry entry) {
        StringWriter traceString = new StringWriter();
        PrintWriter writer = new PrintWriter(traceString);
        entry.error().printStackTrace(writer);
        String trace = traceString.toString();
        writer.flush();
        writer.close();
        return VersionedText.concatDelimited(
                VersionedText.SPACE,
                generateInfoAction(),
                Texts.bracketed(VersionedText.withStyle(
                        VersionedText.translatable("neruina.copy_crash"),
                        style -> style.withColor(Formatting.GOLD)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, trace))
                                .withHoverEvent(new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        VersionedText.translatable("neruina.copy_crash.tooltip")
                                ))
                )),
                Texts.bracketed(VersionedText.withStyle(
                        VersionedText.translatable("neruina.report"),
                        style -> style.withColor(Formatting.LIGHT_PURPLE)
                                .withClickEvent(new ClickEvent(
                                        ClickEvent.Action.RUN_COMMAND,
                                        "/neruina report %s".formatted(entry.uuid())
                                ))
                                .withHoverEvent(new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        VersionedText.translatable("neruina.report.tooltip")
                                ))
                ))
        );
    }

    public Text generateTeleportAction(ErroredType type, BlockPos pos) {
        return Texts.bracketed(VersionedText.withStyle(
                VersionedText.translatable("neruina.teleport"),
                style -> style.withColor(Formatting.DARK_AQUA)
                        .withClickEvent(new ClickEvent(
                                ClickEvent.Action.RUN_COMMAND,
                                "/tp @s %s".formatted(posAsNums(pos))
                        ))
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                VersionedText.translatable("neruina.teleport.%s.tooltip".formatted(type.getName()))
                        ))
        ));
    }

    public Text generateInfoAction() {
        return Texts.bracketed(VersionedText.withStyle(
                VersionedText.translatable("neruina.info"),
                style -> style.withColor(Formatting.GREEN)
                        .withClickEvent(new ClickEvent(
                                ClickEvent.Action.OPEN_URL,
                                "https://github.com/Bawnorton/Neruina/wiki/What-Is-This%3F"
                        ))
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                VersionedText.translatable("neruina.info.tooltip")
                        ))
        ));
    }

    public Text generateHandlingActions(ErroredType type, BlockPos pos) {
        return generateHandlingActions(type, pos, null);
    }

    public Text generateHandlingActions(ErroredType type, BlockPos pos, @Nullable UUID uuid) {
        return VersionedText.concatDelimited(
                VersionedText.SPACE,
                generateTeleportAction(type, pos),
                generateResumeAction(type, uuid != null ? uuid.toString() : posAsNums(pos))
        );
    }

    public Text generateResumeAction(ErroredType type, String args) {
        return Texts.bracketed(VersionedText.withStyle(
                VersionedText.translatable("neruina.try_resume"),
                style -> style.withColor(Formatting.YELLOW)
                        .withClickEvent(new ClickEvent(
                                ClickEvent.Action.RUN_COMMAND,
                                "/neruina resume %s %s".formatted(type.getName(), args)
                        ))
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                VersionedText.translatable("neruina.try_resume.%s.tooltip".formatted(type.getName()))
                        ))
        ));
    }

    public void sendToPlayer(PlayerEntity player, Text message, @Nullable Text... actions) {
        player.sendMessage(VersionedText.pad(VersionedText.concatDelimited(
                VersionedText.LINE_BREAK,
                VersionedText.format(message),
                actions != null ? VersionedText.concatDelimited(VersionedText.LINE_BREAK, actions) : null
        )), false);
    }

    public void sendFormattedMessage(Consumer<Text> sender, String key, Object... args) {
        sender.accept(formatText(key, args));
    }

    public Text formatText(String key, Object... args) {
        return VersionedText.format(VersionedText.translatable(key, args));
    }

    public String posAsNums(BlockPos pos) {
        return "%s %s %s".formatted(pos.getX(), pos.getY(), pos.getZ());
    }
}
