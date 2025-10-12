package com.bawnorton.neruina.handler;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.config.Config;
import com.bawnorton.neruina.thread.ConditionalRunnable;
import com.bawnorton.neruina.util.ErroredType;
import com.bawnorton.neruina.util.TickingEntry;
import com.bawnorton.neruina.version.Texter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MessageHandler {
	public void broadcastToPlayers(MinecraftServer server, Component message, ActionGetter getter) {
		ConditionalRunnable.create(
				() -> {
					int permissionLevel = Config.minPermissionLevelForMessages;
					if (permissionLevel < 0) return;

					server.getPlayerList()
							.getPlayers()
							.stream()
							.filter(player -> player.hasPermissions(permissionLevel))
							.forEach(player -> {
								Component actions = getter.get(player);
								player.sendSystemMessage(
										Texter.pad(
												Texter.concatDelimited(
														Texter.LINE_BREAK,
														message,
														actions
												)
										), false
								);
							});
				}, () -> server.getPlayerList().getPlayerCount() > 0
		);
	}

	public void broadcastToPlayers(MinecraftServer server, Component message) {
		broadcastToPlayers(server, message, p -> Texter.empty());
	}

	public void sendToPlayer(Player player, Component message, @Nullable Component... actions) {
		sendToPlayer(player, message, true, actions);
	}

	public void sendToPlayer(Player player, Component message, boolean pad, @Nullable Component... actions) {
		message = Texter.concatDelimited(
				Texter.LINE_BREAK,
				Texter.format(message),
				actions != null ? Texter.concatDelimited(Texter.LINE_BREAK, actions) : null
		);
		player.displayClientMessage(pad ? Texter.pad(message) : message, false);
	}

	public Component generateEntityActions(Player forPlayer, Entity entity) {
		return Texter.concatDelimited(
				Texter.SPACE,
				generateHandlingActions(forPlayer, ErroredType.ENTITY, entity.level().dimension(), entity.getOnPos(), entity.getUUID()),
				generateKillAction(forPlayer, entity.getUUID())
		);
	}

	public Component generateResourceActions(Player forPlayer, TickingEntry entry) {
		return Texter.concatDelimited(
				Texter.SPACE,
				generateInfoAction(),
				generateCopyCrashAction(entry),
				generateReportAction(forPlayer, entry)
		);
	}

	public Component generateHandlingActions(Player forPlayer, ErroredType type, ResourceKey<Level> dimension, BlockPos pos) {
		return generateHandlingActions(forPlayer, type, dimension, pos, null);
	}

	public Component generateHandlingActions(Player forPlayer, ErroredType type, ResourceKey<Level> dimension, BlockPos pos, @Nullable UUID uuid) {
		return Texter.concatDelimited(
				Texter.SPACE,
				generateTeleportAction(forPlayer, type, dimension, pos),
				generateResumeAction(forPlayer, type, uuid != null ? uuid.toString() : posAsNums(pos))
		);
	}

	public Component generateKillAction(Player forPlayer, UUID uuid) {
		return generateCommandAction(forPlayer, "neruina.kill", ChatFormatting.DARK_RED, "/neruina kill %s".formatted(uuid));
	}

	public Component generateCopyCrashAction(TickingEntry entry) {
		StringWriter traceString = new StringWriter();
		PrintWriter writer = new PrintWriter(traceString);
		entry.error().printStackTrace(writer);
		String trace = traceString.toString();
		writer.flush();
		writer.close();
		return generateAction("neruina.copy_crash", ChatFormatting.GOLD, ClickEvent.Action.COPY_TO_CLIPBOARD, trace);
	}

	public Component generateReportAction(Player forPlayer, TickingEntry entry) {
		return generateCommandAction(
				forPlayer, "neruina.report",
				ChatFormatting.LIGHT_PURPLE,
				"/neruina report %s".formatted(entry.uuid())
		);
	}

	public Component generateTeleportAction(Player forPlayer, ErroredType type, ResourceKey<Level> dimension, BlockPos pos) {
		return generateCommandAction(
				forPlayer, "neruina.teleport",
				"neruina.teleport.%s.tooltip".formatted(type.getName()),
				ChatFormatting.DARK_AQUA,
				"/execute in %s run tp @s %s".formatted(dimension.location().toString(), posAsNums(pos))
		);
	}

	public Component generateInfoAction() {
		return generateAction(
				"neruina.info",
				ChatFormatting.GREEN,
				ClickEvent.Action.OPEN_URL,
				"https://github.com/Bawnorton/Neruina/wiki/What-Is-This%3F"
		);
	}

	public Component generateResumeAction(Player forPlayer, ErroredType type, String args) {
		return generateCommandAction(
				forPlayer, "neruina.try_resume",
				"neruina.try_resume.%s.tooltip".formatted(type.getName()),
				ChatFormatting.YELLOW,
				"/neruina resume %s %s".formatted(type.getName(), args)
		);
	}

	public Component generateClearAction(Player forPlayer) {
		return generateCommandAction(forPlayer, "neruina.clear", ChatFormatting.BLUE, "/neruina clear_tracked");
	}

	public Component generateOpenReportAction(String url) {
		return generateAction("neruina.open_report", ChatFormatting.LIGHT_PURPLE, ClickEvent.Action.OPEN_URL, url);
	}

	public Component generateCancelLoginAction(Player forPlayer) {
		return generateCommandAction(forPlayer, "neruina.cancel", ChatFormatting.DARK_RED, "/neruina cancel_login");
	}

	private Component generateCommandAction(Player forPlayer, String key, ChatFormatting color, String command) {
		return generateCommandAction(forPlayer, key, "%s.tooltip".formatted(key), color, command);
	}

	private Component generateCommandAction(Player forPlayer, String key, String hoverKey, ChatFormatting color, String command) {
		if (forPlayer.hasPermissions(Config.minPermissionLevelForCommands)) {
			return generateAction(key, hoverKey, color, ClickEvent.Action.RUN_COMMAND, command);
		} else {
			return Texter.empty();
		}
	}

	@SuppressWarnings("SameParameterValue")
	private Component generateCommandAction(Player forPlayer, Component message, String hoverKey, ChatFormatting color, String command) {
		return generateCommandAction(forPlayer, message, Texter.translatable(hoverKey), color, command);
	}

	private Component generateCommandAction(Player forPlayer, Component message, Component hoverMessage, ChatFormatting color, String command) {
		if (forPlayer.hasPermissions(Config.minPermissionLevelForCommands)) {
			return generateAction(message, hoverMessage, color, ClickEvent.Action.RUN_COMMAND, command);
		} else {
			return Texter.empty();
		}
	}

	private Component generateAction(String key, ChatFormatting color, ClickEvent.Action action, String value) {
		return generateAction(key, "%s.tooltip".formatted(key), color, action, value);
	}

	private Component generateAction(String key, String hoverKey, ChatFormatting color, ClickEvent.Action action, String value) {
		return generateAction(Texter.translatable(key), Texter.translatable(hoverKey), color, action, value);
	}

	private Component generateAction(Component message, Component hoverMessage, ChatFormatting color, ClickEvent.Action action, String value) {
		return ComponentUtils.wrapInSquareBrackets(Texter.withStyle(
				message,
				style -> style.withColor(color)
						.withClickEvent(Texter.clickEvent(action, value))
						.withHoverEvent(Texter.hoverEvent(HoverEvent.Action.SHOW_TEXT, hoverMessage))
		));
	}

	public Component generateSuspendedInfo(Player forPlayer) {
		TickHandler tickHandler = Neruina.getInstance().getTickHandler();
		List<Component> tickingEntryMessages = new ArrayList<>();
		int count = tickHandler.getTickingEntries().size();

		if (count == 1) {
			tickingEntryMessages.add(formatText("neruina.ticking_entries.count.single"));
		} else {
			tickingEntryMessages.add(formatText("neruina.ticking_entries.count", count));
		}
		tickHandler.getTickingEntries().forEach(entry -> tickingEntryMessages.add(
				generateCommandAction(
						forPlayer, Texter.translatable(
								"neruina.ticking_entries.entry",
								entry.getCauseName(),
								posAsNums(entry.pos())
						),
						"neruina.ticking_entries.entry.tooltip",
						ChatFormatting.DARK_RED,
						"/neruina info %s".formatted(entry.uuid())
				)
		));
		tickingEntryMessages.add(Texter.concatDelimited(
				Texter.SPACE,
				generateInfoAction(),
				generateClearAction(forPlayer)
		));
		return Texter.concatDelimited(Texter.LINE_BREAK, tickingEntryMessages.toArray(new Component[0]));
	}

	public Component formatText(String key, Object... args) {
		return Texter.format(Texter.translatable(key, args));
	}

	public String posAsNums(BlockPos pos) {
		return "%s %s %s".formatted(pos.getX(), pos.getY(), pos.getZ());
	}

	public interface ActionGetter {
		Component get(Player player);
	}
}
