package com.bawnorton.neruina.handler;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.config.Config;
import com.bawnorton.neruina.thread.ConditionalRunnable;
import com.bawnorton.neruina.util.ErroredType;
import com.bawnorton.neruina.util.TickingEntry;
import com.bawnorton.neruina.version.Texter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//? if >1.19.2 {
import net.minecraft.registry.RegistryKey;
//?} else {
/*import net.minecraft.util.registry.RegistryKey;
*///?}

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
                Texter.pad(Texter.concatDelimited(Texter.LINE_BREAK, messages))
        );
    }

    public void sendToPlayer(PlayerEntity player, Text message, @Nullable Text... actions) {
        sendToPlayer(player, message, true, actions);
    }

    public void sendToPlayer(PlayerEntity player, Text message, boolean pad, @Nullable Text... actions) {
        message = Texter.concatDelimited(
                Texter.LINE_BREAK,
                Texter.format(message),
                actions != null ? Texter.concatDelimited(Texter.LINE_BREAK, actions) : null
        );
        player.sendMessage(pad ? Texter.pad(message) : message, false);
    }

    public Text generateEntityActions(Entity entity) {
        return Texter.concatDelimited(
                Texter.SPACE,
                generateHandlingActions(ErroredType.ENTITY, entity.getWorld().getRegistryKey(), entity.getBlockPos(), entity.getUuid()),
                generateKillAction(entity.getUuid())
        );
    }

    public Text generateResourceActions(TickingEntry entry) {
        return Texter.concatDelimited(
                Texter.SPACE,
                generateInfoAction(),
                generateCopyCrashAction(entry),
                generateReportAction(entry)
        );
    }

    public Text generateHandlingActions(ErroredType type, RegistryKey<World> dimension, BlockPos pos) {
        return generateHandlingActions(type, dimension, pos, null);
    }

    public Text generateHandlingActions(ErroredType type, RegistryKey<World> dimension, BlockPos pos, @Nullable UUID uuid) {
        return Texter.concatDelimited(
                Texter.SPACE,
                generateTeleportAction(type, dimension, pos),
                generateResumeAction(type, uuid != null ? uuid.toString() : posAsNums(pos))
        );
    }

    public Text generateKillAction(UUID uuid) {
        return generateCommandAction("neruina.kill", Formatting.DARK_RED, "/neruina kill %s".formatted(uuid));
    }

    public Text generateCopyCrashAction(TickingEntry entry) {
        StringWriter traceString = new StringWriter();
        PrintWriter writer = new PrintWriter(traceString);
        entry.error().printStackTrace(writer);
        String trace = traceString.toString();
        writer.flush();
        writer.close();
        return generateAction("neruina.copy_crash", Formatting.GOLD, ClickEvent.Action.COPY_TO_CLIPBOARD, trace);
    }

    public Text generateReportAction(TickingEntry entry) {
        return generateCommandAction(
                "neruina.report",
                Formatting.LIGHT_PURPLE,
                "/neruina report %s".formatted(entry.uuid())
        );
    }

    public Text generateTeleportAction(ErroredType type, RegistryKey<World> dimension, BlockPos pos) {
        return generateCommandAction(
                "neruina.teleport",
                "neruina.teleport.%s.tooltip".formatted(type.getName()),
                Formatting.DARK_AQUA,
                "/execute in %s run tp @s %s".formatted(dimension.getValue().toString(), posAsNums(pos))
        );
    }

    public Text generateInfoAction() {
        return generateAction(
                "neruina.info",
                Formatting.GREEN,
                ClickEvent.Action.OPEN_URL,
                "https://github.com/Bawnorton/Neruina/wiki/What-Is-This%3F"
        );
    }

    public Text generateResumeAction(ErroredType type, String args) {
        return generateCommandAction(
                "neruina.try_resume",
                "neruina.try_resume.%s.tooltip".formatted(type.getName()),
                Formatting.YELLOW,
                "/neruina resume %s %s".formatted(type.getName(), args)
        );
    }

    public Text generateClearAction() {
        return generateCommandAction("neruina.clear", Formatting.BLUE, "/neruina clear_tracked");
    }

    public Text generateOpenReportAction(String url) {
        return generateAction("neruina.open_report", Formatting.LIGHT_PURPLE, ClickEvent.Action.OPEN_URL, url);
    }

    public Text generateCancelLoginAction() {
        return generateCommandAction("neruina.cancel", Formatting.DARK_RED, "/neruina cancel_login");
    }

    private Text generateCommandAction(String key, Formatting color, String command) {
        return generateCommandAction(key, "%s.tooltip".formatted(key), color, command);
    }

    private Text generateCommandAction(String key, String hoverKey, Formatting color, String command) {
        return generateAction(key, hoverKey, color, ClickEvent.Action.RUN_COMMAND, command);
    }

    @SuppressWarnings("SameParameterValue")
    private Text generateCommandAction(Text message, String hoverKey, Formatting color, String command) {
        return generateCommandAction(message, Texter.translatable(hoverKey), color, command);
    }

    private Text generateCommandAction(Text message, Text hoverMessage, Formatting color, String command) {
        return generateAction(message, hoverMessage, color, ClickEvent.Action.RUN_COMMAND, command);
    }

    private Text generateAction(String key, Formatting color, ClickEvent.Action action, String value) {
        return generateAction(key, "%s.tooltip".formatted(key), color, action, value);
    }

    private Text generateAction(String key, String hoverKey, Formatting color, ClickEvent.Action action, String value) {
        return generateAction(Texter.translatable(key), Texter.translatable(hoverKey), color, action, value);
    }

    private Text generateAction(Text message, Text hoverMessage, Formatting color, ClickEvent.Action action, String value) {
        return Texts.bracketed(Texter.withStyle(
                message,
                style -> style.withColor(color)
                        .withClickEvent(new ClickEvent(action, value))
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                hoverMessage
                        ))
        ));
    }

    public Text generateSuspendedInfo() {
        TickHandler tickHandler = Neruina.getInstance().getTickHandler();
        List<Text> tickingEntryMessages = new ArrayList<>();
        int count = tickHandler.getTickingEntries().size();

        if(count == 1) {
            tickingEntryMessages.add(formatText("neruina.ticking_entries.count.single"));
        } else {
            tickingEntryMessages.add(formatText("neruina.ticking_entries.count", count));
        }
        tickHandler.getTickingEntries().forEach(entry -> tickingEntryMessages.add(
                generateCommandAction(
                        Texter.translatable(
                                "neruina.ticking_entries.entry",
                                entry.getCauseName(),
                                posAsNums(entry.pos())
                        ),
                        "neruina.ticking_entries.entry.tooltip",
                        Formatting.DARK_RED,
                        "/neruina info %s".formatted(entry.uuid())
                )
        ));
        tickingEntryMessages.add(Texter.concatDelimited(
                Texter.SPACE,
                generateInfoAction(),
                generateClearAction()
        ));
        return Texter.concatDelimited(Texter.LINE_BREAK, tickingEntryMessages.toArray(new Text[0]));
    }

    public Text formatText(String key, Object... args) {
        return Texter.format(Texter.translatable(key, args));
    }

    public String posAsNums(BlockPos pos) {
        return "%s %s %s".formatted(pos.getX(), pos.getY(), pos.getZ());
    }
}
