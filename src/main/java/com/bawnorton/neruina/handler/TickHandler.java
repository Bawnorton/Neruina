package com.bawnorton.neruina.handler;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.config.Config;
import com.bawnorton.neruina.exception.TickingException;
import com.bawnorton.neruina.extend.Errorable;
import com.bawnorton.neruina.handler.client.ClientTickHandler;
import com.bawnorton.neruina.mixin.accessor.LevelChunkAccessor;
import com.bawnorton.neruina.platform.Platform;
import com.bawnorton.neruina.util.ErroredType;
import com.bawnorton.neruina.util.MultiSetMap;
import com.bawnorton.neruina.util.TickingEntry;
import com.bawnorton.neruina.version.Texter;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;


public final class TickHandler {
	private final List<TickingEntry> recentErrors = new ArrayList<>();
	private final Map<UUID, TickingEntry> tickingEntries = new HashMap<>();
	private final MultiSetMap<BlockState, BlockPos> erroredBlockStates = new MultiSetMap<>();
	private int stopwatch = 0;

	public void tick() {
		stopwatch++;
		if (stopwatch >= 600) {
			if (!recentErrors.isEmpty()) {
				//? if >1.20.1 {
				recentErrors.removeFirst();
				//?} else {
				/*recentErrors.remove(0);
				*///?}
			}
			stopwatch = 0;
		}
	}

	public void init() {
		tickingEntries.clear();
		recentErrors.clear();
	}

	@SuppressWarnings("unused")
	public void safelyTickItemStack(ItemStack instance, Level level, Entity entity, EquipmentSlot slot, int slotIndex, Operation<Void> original) {
		try {
			if (isErrored(instance)) {
				return;
			}
			original.call(instance, level, entity, slot);
		} catch (Throwable e) {
			handleTickingItemStack(e, instance, !level.isClientSide(), (Player) entity, slotIndex);
		}
	}

	@SuppressWarnings("unused")
	public void safelyTickItemStack(ItemStack instance, Level level, Entity entity, int slot, boolean selected, Operation<Void> original) {
		try {
			if (isErrored(instance)) {
				return;
			}
			original.call(instance, level, entity, slot, selected);
		} catch (Throwable e) {
			handleTickingItemStack(e, instance, !level.isClientSide(), (Player) entity, slot);
		}
	}

	@SuppressWarnings("unused")
	public void safelyTickItemStack(ItemStack instance, Level level, Player player, int slot, int selected, Operation<Void> original) {
		try {
			if (isErrored(instance)) {
				return;
			}
			original.call(instance, level, player, slot, selected);
		} catch (Throwable e) {
			handleTickingItemStack(e, instance, !level.isClientSide(), player, slot);
		}
	}

	public void safelyTickEntities(Consumer<Object> instance, Entity entity, Operation<Void> original) {
		try {
			if (isErrored(entity)) {
				handleErroredEntity(entity);
				return;
			}
			original.call(instance, entity);
		} catch (TickingException e) {
			throw e;
		} catch (Throwable e) {
			checkBlacklistAndHandle(entity, e);
		}
	}

	public <T extends Entity> void safelyTickEntities(Consumer<T> consumer, T entity, Level level, Object random, Operation<Void> original) {
		try {
			if (isErrored(entity)) {
				handleErroredEntity(entity);
				return;
			}
			original.call(consumer, entity, level, random);
		} catch (TickingException e) {
			throw e;
		} catch (Throwable e) {
			checkBlacklistAndHandle(entity, e);
		}
	}

	public void safelyTickPlayer(ServerPlayer instance, Operation<Void> original) {
		try {
			original.call(instance);
		} catch (Throwable e) {
			if (!Config.handleTickingPlayers) {
				throw TickingException.notHandled("handle_ticking_players", e);
			}
			handleTickingPlayer(instance, e);
		}
	}

	public void safelyTickBlockState(BlockState instance, ServerLevel level, BlockPos pos, Object random, Operation<Void> original) {
		try {
			if (isErrored(instance, pos)) {
				return;
			}
			original.call(instance, level, pos, random);
		} catch (Throwable e) {
			if (!Config.handleTickingBlockStates) {
				throw TickingException.notHandled("handle_ticking_block_states", e);
			}
			Identifier blockId = BuiltInRegistries.BLOCK.getKey(instance.getBlock());
			Identifier owningBlacklist = getBlacklistFor(ErroredType.BLOCK_STATE, blockId);
			if (owningBlacklist != null) {
				throw TickingException.blacklisted(owningBlacklist, blockId, e);
			}
			MessageHandler messageHandler = Neruina.getInstance().getMessageHandler();
			Component message = messageHandler.formatText(
					"neruina.ticking.block_state",
					instance.getBlock().getName().getString(),
					messageHandler.posAsNums(pos)
			);
			Neruina.LOGGER.warn("Neruina Caught An Exception, see below for cause", e);
			addErrored(instance, pos);
			TickingEntry tickingEntry = new TickingEntry(instance, true, level.dimension(), pos, e);
			trackError(tickingEntry);
			messageHandler.broadcastToPlayers(
					level.getServer(),
					message,
					forPlayer -> Texter.concatDelimited(
							Texter.LINE_BREAK,
							messageHandler.generateHandlingActions(forPlayer, ErroredType.BLOCK_STATE, level.dimension(), pos),
							messageHandler.generateResourceActions(forPlayer, tickingEntry)
					)
			);
		}
	}

	public void safelyTickBlockEntity(BlockEntityTicker<? extends BlockEntity> instance, Level level, BlockPos pos, BlockState state, BlockEntity blockEntity, Operation<Void> original) {
		try {
			if (isErrored(blockEntity)) {
				if (level.isClientSide()) {
					return;
				}

				LevelChunk chunk = level.getChunkAt(pos);
				((LevelChunkAccessor) chunk).neruina$removeBlockEntityTicker(pos);
				return;
			}
			original.call(instance, level, pos, state, blockEntity);
		} catch (Throwable e) {
			if (!Config.handleTickingBlockEntities) {
				throw TickingException.notHandled("handle_ticking_block_entities", e);
			}
			Identifier blockEntityId = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType());
			Identifier owningBlacklist = getBlacklistFor(ErroredType.BLOCK_ENTITY, blockEntityId);
			if (owningBlacklist != null) {
				throw TickingException.blacklisted(owningBlacklist, blockEntityId, e);
			}
			MessageHandler messageHandler = Neruina.getInstance().getMessageHandler();
			Component message = messageHandler.formatText(
					"neruina.ticking.block_entity",
					state.getBlock().getName().getString(),
					messageHandler.posAsNums(pos)
			);
			Neruina.LOGGER.warn("Neruina caught an exception, see below for cause", e);
			addErrored(blockEntity);
			if (!level.isClientSide()) {
				TickingEntry tickingEntry = new TickingEntry(blockEntity, true, level.dimension(), pos, e);
				trackError(blockEntity, tickingEntry);
				messageHandler.broadcastToPlayers(
						level.getServer(),
						message,
						forPlayer -> Texter.concatDelimited(
								Texter.LINE_BREAK,
								messageHandler.generateHandlingActions(forPlayer, ErroredType.BLOCK_ENTITY, level.dimension(), pos),
								messageHandler.generateResourceActions(forPlayer, tickingEntry)
						)
				);
			}
		}
	}

	private void checkBlacklistAndHandle(Entity entity, Throwable e) {
		if (!Config.handleTickingEntities) {
			throw TickingException.notHandled("handle_ticking_entities", e);
		}
		Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
		Identifier owningBlacklist = getBlacklistFor(ErroredType.ENTITY, entityId);
		if (owningBlacklist != null) {
			throw TickingException.blacklisted(owningBlacklist, entityId, e);
		}
		handleTickingEntity(entity, e);
	}

	private void handleTickingItemStack(Throwable e, ItemStack instance, boolean isServer, Player player, int slot) {
		if (!Config.handleTickingItemStacks) {
			throw TickingException.notHandled("handle_ticking_item_stacks", e);
		}
		Identifier itemId = BuiltInRegistries.ITEM.getKey(instance.getItem());
		Identifier owningBlacklist = getBlacklistFor(ErroredType.ITEM_STACK, itemId);
		if (owningBlacklist != null) {
			throw TickingException.blacklisted(owningBlacklist, itemId, e);
		}
		Neruina.LOGGER.warn("Neruina caught an exception, see below for cause", e);
		addErrored(instance);
		if (isServer) {
			TickingEntry tickingEntry = new TickingEntry(instance, false, player.level().dimension(), player.getOnPos(), e);
			trackError(instance, tickingEntry);
			MessageHandler messageHandler = Neruina.getInstance().getMessageHandler();
			messageHandler.sendToPlayer(
					player,
					Texter.translatable("neruina.ticking.item_stack", instance.getHoverName().getString(), slot),
					messageHandler.generateResumeAction(player, ErroredType.ITEM_STACK, player.getStringUUID()),
					messageHandler.generateResourceActions(player, tickingEntry)
			);
		}
	}

	private void handleErroredEntity(Entity entity) {
		try {
			if (entity instanceof Player) {
				return;
			}
			if (entity.level().isClientSide()) {
				return;
			}

			entity.baseTick();
			if (Config.autoKillTickingEntities || !entity.isAlive()) {
				killEntity(entity, null);
			}
		} catch (Throwable e) {
			try {
				killEntity(entity, Neruina.getInstance().getMessageHandler().formatText("neruina.ticking.entity.suspend_failed", entity.getName().getString()));
			} catch (Throwable ex) {
				TickingException exception = new TickingException("Failed to handle errored entity, deliberately crashing to prevent further issues.", ex);
				exception.addSuppressed(e);
				throw exception;
			}
		}
	}

	public void killEntity(Entity entity, @Nullable Component withMessage) {
		//? if >1.21.1 {
		if (entity.level() instanceof ServerLevel serverWorld) {
			entity.kill(serverWorld);
		}
		//?} else {
		/*entity.kill();
		 *///?}
		entity.remove(Entity.RemovalReason.KILLED); // Necessary for any living entity
		removeErrored(entity);
		if (withMessage != null) {
			Neruina.getInstance().getMessageHandler().broadcastToPlayers(entity.level().getServer(), withMessage);
		}
	}

	private void handleTickingEntity(Entity entity, Throwable e) {
		if (entity instanceof Player player) {
			if (player instanceof ServerPlayer serverPlayer) {
				handleTickingPlayer(serverPlayer, e);
			} else {
				handleTickingClient(player, e);
			}
			return;
		}

		Neruina.LOGGER.warn("Neruina caught an exception, see below for cause", e);
		addErrored(entity);
		Level level = entity.level();
		if (!level.isClientSide()) {
			BlockPos pos = entity.getOnPos();
			TickingEntry tickingEntry = new TickingEntry(entity, true, level.dimension(), pos, e);
			trackError(entity, tickingEntry);
			MessageHandler messageHandler = Neruina.getInstance().getMessageHandler();
			Component message = messageHandler.formatText(
					"neruina.ticking.entity.%s".formatted(
							Config.autoKillTickingEntities
									? "killed" : "suspended"
					),
					entity.getName().getString(),
					messageHandler.posAsNums(pos)
			);
			messageHandler.broadcastToPlayers(
					entity.level().getServer(), message, forPlayer -> {
						Component actions = messageHandler.generateResourceActions(forPlayer, tickingEntry);
						if (!Config.autoKillTickingEntities) {
							actions = Texter.concatDelimited(
									Texter.LINE_BREAK,
									messageHandler.generateEntityActions(forPlayer, entity),
									actions
							);
						}
						return actions;
					}
			);
		}
	}

	private void handleTickingPlayer(ServerPlayer player, Throwable e) {
		Neruina.LOGGER.warn("Neruina caught an exception, see below for cause", e);
		MinecraftServer server = player.level().getServer();
		String name = player.getDisplayName().getString();
		MessageHandler messageHandler = Neruina.getInstance().getMessageHandler();
		Component message = messageHandler.formatText("neruina.ticking.player", name);
		TickingEntry tickingEntry = new TickingEntry(player, false, player.level().dimension(), player.getOnPos(), e);
		trackError(tickingEntry);
		messageHandler.broadcastToPlayers(server, message, forPlayer -> messageHandler.generateResourceActions(forPlayer, tickingEntry));
		try {
			player.connection.disconnect(
					Texter.concat(
							Texter.translatable("neruina.kick.message"),
							Texter.translatable("neruina.kick.reason")
					)
			);
		} catch (NullPointerException ex) {
			Neruina.LOGGER.error("Neruina caught an exception on a player, but the player is not connected, this should not happen. Behaviour is undefined.", ex);
		}
	}

	private void handleTickingClient(Player player, Throwable e) {
		if (player.level().isClientSide() || Platform.isClient()) {
			ClientTickHandler.handleTickingClient(player, e);
		} else {
			Neruina.LOGGER.error("Neruina caught an exception, but the player is not a server player, this should not happen. Behaviour is undefined.", e);
		}
	}

	private void trackError(TickingEntry entry) {
		trackError(null, entry);
	}

	private void trackError(Object object, TickingEntry entry) {
		if (object instanceof Errorable errorable) {
			trackError(errorable, entry);
		} else if (object == null) {
			trackError(null, entry);
		}
	}

	private void trackError(@Nullable Errorable errorable, TickingEntry entry) {
		recentErrors.add(entry);
		addTickingEntry(entry);
		if (errorable != null) {
			errorable.neruina$setTickingEntryId(entry.uuid());
		}
		if (Config.tickingExceptionThreshold != -1 && recentErrors.size() >= Config.tickingExceptionThreshold) {
			CrashReport report = CrashReport.forThrowable(
					new RuntimeException("Too Many Ticking Exceptions"),
					"Neruina has caught too many ticking exceptions in a short period of time, something is very wrong, see below for more info"
			);
			CrashReportCategory header = report.addCategory("Information");
			header.setDetail(
					"Threshold",
					"%d, set \"ticking_exception_threshold\" to -1 to disable.".formatted(
							Config.tickingExceptionThreshold
					)
			);
			header.setDetail("Caught", recentErrors.size());
			String wiki = "https://github.com/Bawnorton/Neruina/wiki/Too-Many-Ticking-Exceptions";
			String lines = "=".repeat(wiki.length() + "Wiki".length() + 2);
			header.setDetail("", lines);
			header.setDetail("Wiki", wiki);
			header.setDetail("", lines);
			for (int i = 0; i < recentErrors.size(); i++) {
				TickingEntry error = recentErrors.get(i);
				CrashReportCategory category = report.addCategory("Ticking Exception #%s - (%s: %s)".formatted(i + 1, error.getCauseType(), error.getCauseName()));
				error.populate(category);
			}
			throw new ReportedException(report);
		}
	}

	public boolean isErrored(Object obj) {
		if (obj instanceof Errorable errorable) {
			return errorable.neruina$isErrored();
		}
		return false;
	}

	public boolean isErrored(BlockState state, BlockPos pos) {
		return erroredBlockStates.contains(state, pos);
	}

	private void addErrored(Object obj) {
		if (obj instanceof Errorable errorable) {
			errorable.neruina$setErrored();
		}
	}

	private void addErrored(BlockState state, BlockPos pos) {
		erroredBlockStates.put(state, pos);
	}

	public void removeErrored(Object obj) {
		if (obj instanceof Errorable errorable) {
			errorable.neruina$clearErrored();
			tickingEntries.remove(errorable.neruina$getTickingEntryId());
		}
	}

	public void removeErrored(BlockState state, BlockPos pos) {
		erroredBlockStates.remove(state, pos);
	}

	public @Nullable TickingEntry getTickingEntry(UUID uuid) {
		return tickingEntries.get(uuid);
	}

	public Collection<TickingEntry> getTickingEntries() {
		return tickingEntries.values();
	}

	public void addTickingEntry(TickingEntry entry) {
		Object cause = entry.getCause();
		boolean shouldAdd = false;
		if (isErrored(cause)) {
			shouldAdd = true;
		} else if (cause instanceof BlockState state) {
			shouldAdd = isErrored(state, entry.pos());
		}
		if (shouldAdd) {
			tickingEntries.put(entry.uuid(), entry);
		}
	}

	public void addTickingEntryUnsafe(TickingEntry entry) {
		tickingEntries.put(entry.uuid(), entry);
	}

	public Optional<UUID> getTickingEntryId(Object obj) {
		if (obj instanceof Errorable errorable && errorable.neruina$isErrored()) {
			return Optional.ofNullable(errorable.neruina$getTickingEntryId());
		}
		return Optional.empty();
	}

	public int clearTracked() {
		int size = tickingEntries.size();
		tickingEntries.clear();
		recentErrors.clear();
		return size;
	}

	private Identifier getBlacklistFor(ErroredType type, Identifier id) {
		return Neruina.getInstance().getBlacklistHandler().getBlacklistFor(type, id);
	}
}