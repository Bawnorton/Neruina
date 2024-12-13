package com.bawnorton.neruina.handler;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.config.Config;
import com.bawnorton.neruina.exception.TickingException;
import com.bawnorton.neruina.extend.Errorable;
import com.bawnorton.neruina.handler.client.ClientTickHandler;
import com.bawnorton.neruina.mixin.accessor.WorldChunkAccessor;
import com.bawnorton.neruina.platform.Platform;
import com.bawnorton.neruina.util.ErroredType;
import com.bawnorton.neruina.util.MultiSetMap;
import com.bawnorton.neruina.util.TickingEntry;
import com.bawnorton.neruina.version.Texter;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
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
                recentErrors.remove(0);
            }
            stopwatch = 0;
        }
    }

    public void init() {
        tickingEntries.clear();
        recentErrors.clear();
    }

    @SuppressWarnings("unused")
    public void safelyTickItemStack(ItemStack instance, World world, Entity entity, int slot, boolean selected, Operation<Void> original) {
        try {
            if (isErrored(instance)) {
                return;
            }
            original.call(instance, world, entity, slot, selected);
        } catch (Throwable e) {
            handleTickingItemStack(e, instance, !world.isClient(), (PlayerEntity) entity, slot);
        }
    }

    @SuppressWarnings("unused")
    public void safelyTickItemStack(ItemStack instance, World world, PlayerEntity player, int slot, int selected, Operation<Void> original) {
        try {
            if (isErrored(instance)) {
                return;
            }
            original.call(instance, world, player, slot, selected);
        } catch (Throwable e) {
            handleTickingItemStack(e, instance, !world.isClient(), player, slot);
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
            if (!Config.getInstance().handleTickingEntities) {
                throw TickingException.notHandled("handle_ticking_entities", e);
            }
            handleTickingEntity(entity, e);
        }
    }

    public <T extends Entity> void safelyTickEntities(Consumer<T> consumer, T entity, World world, Object random, Operation<Void> original) {
        try {
            if (isErrored(entity)) {
                handleErroredEntity(entity);
                return;
            }
            original.call(consumer, entity, world, random);
        } catch (TickingException e) {
            throw e;
        } catch (Throwable e) {
            if (!Config.getInstance().handleTickingEntities) {
                throw TickingException.notHandled("handle_ticking_entities", e);
            }
            handleTickingEntity(entity, e);
        }
    }

    public void safelyTickPlayer(ServerPlayerEntity instance, Operation<Void> original) {
        try {
            original.call(instance);
        } catch (Throwable e) {
            if (!Config.getInstance().handleTickingPlayers) {
                throw TickingException.notHandled("handle_ticking_players", e);
            }
            handleTickingPlayer(instance, e);
        }
    }

    public void safelyTickBlockState(BlockState instance, ServerWorld world, BlockPos pos, Object random, Operation<Void> original) {
        try {
            if (isErrored(instance, pos)) {
                return;
            }
            original.call(instance, world, pos, random);
        } catch (Throwable e) {
            if (!Config.getInstance().handleTickingBlockStates) {
                throw TickingException.notHandled("handle_ticking_block_states", e);
            }
            MessageHandler messageHandler = Neruina.getInstance().getMessageHandler();
            Text message = messageHandler.formatText("neruina.ticking.block_state",
                    instance.getBlock().getName().getString(),
                    messageHandler.posAsNums(pos)
            );
            Neruina.LOGGER.warn("Neruina Caught An Exception, see below for cause", e);
            addErrored(instance, pos);
            TickingEntry tickingEntry = new TickingEntry(instance, true, world.getRegistryKey(), pos, e);
            trackError(tickingEntry);
            messageHandler.broadcastToPlayers(world.getServer(),
                    message,
                    messageHandler.generateHandlingActions(ErroredType.BLOCK_STATE, world.getRegistryKey(), pos),
                    messageHandler.generateResourceActions(tickingEntry)
            );
        }
    }

    public void safelyTickBlockEntity(BlockEntityTicker<? extends BlockEntity> instance, World world, BlockPos pos, BlockState state, BlockEntity blockEntity, Operation<Void> original) {
        try {
            if (isErrored(blockEntity)) {
                if (world.isClient()) {
                    return;
                }

                WorldChunk chunk = world.getWorldChunk(pos);
                ((WorldChunkAccessor) chunk).invokeRemoveBlockEntityTicker(pos);
                return;
            }
            original.call(instance, world, pos, state, blockEntity);
        } catch (Throwable e) {
            if (!Config.getInstance().handleTickingBlockEntities) {
                throw TickingException.notHandled("handle_ticking_block_entities", e);
            }
            MessageHandler messageHandler = Neruina.getInstance().getMessageHandler();
            Text message = messageHandler.formatText("neruina.ticking.block_entity",
                    state.getBlock().getName().getString(),
                    messageHandler.posAsNums(pos)
            );
            Neruina.LOGGER.warn("Neruina caught an exception, see below for cause", e);
            addErrored(blockEntity);
            if (!world.isClient()) {
                TickingEntry tickingEntry = new TickingEntry(blockEntity, true, world.getRegistryKey(), pos, e);
                trackError((Errorable) blockEntity, tickingEntry);
                messageHandler.broadcastToPlayers(world.getServer(),
                        message,
                        messageHandler.generateHandlingActions(ErroredType.BLOCK_ENTITY, world.getRegistryKey(), pos),
                        messageHandler.generateResourceActions(tickingEntry)
                );
            }
        }
    }

    private void handleTickingItemStack(Throwable e, ItemStack instance, boolean isServer, PlayerEntity player, int slot) {
        if (!Config.getInstance().handleTickingItemStacks) {
            throw TickingException.notHandled("handle_ticking_item_stacks", e);
        }
        Neruina.LOGGER.warn("Neruina caught an exception, see below for cause", e);
        addErrored(instance);
        if (isServer) {
            TickingEntry tickingEntry = new TickingEntry(instance, false, player.getWorld().getRegistryKey(), player.getBlockPos(), e);
            trackError((Errorable) (Object) instance, tickingEntry);
            MessageHandler messageHandler = Neruina.getInstance().getMessageHandler();
            messageHandler.sendToPlayer(player,
                    Texter.translatable("neruina.ticking.item_stack", instance.getName().getString(), slot),
                    messageHandler.generateResumeAction(ErroredType.ITEM_STACK, player.getUuidAsString()),
                    messageHandler.generateResourceActions(tickingEntry)
            );
        }
    }

    private void handleErroredEntity(Entity entity) {
        try {
            if (entity instanceof PlayerEntity) {
                return;
            }
            if (entity.getWorld().isClient()) {
                return;
            }

            entity.baseTick();
            if (Config.getInstance().autoKillTickingEntities || !entity.isAlive()) {
                killEntity(entity, null);
            }
        } catch (Throwable e) {
            try {
                killEntity(entity, Neruina.getInstance().getMessageHandler().formatText("neruina.ticking.entity.suspend_failed", entity.getName().getString()));
            } catch (Throwable ex) {
                throw new TickingException("Exception occurred while handling errored entity", ex);
            }
        }
    }

    public void killEntity(Entity entity, @Nullable Text withMessage) {
        //? if >1.21.2 {
        if(entity.getWorld() instanceof ServerWorld serverWorld) {
            entity.kill(serverWorld);
        }
        //?} else {
        /*entity.kill();
        *///?}
        entity.remove(Entity.RemovalReason.KILLED); // Necessary for any living entity
        removeErrored(entity);
        if (withMessage != null) {
            Neruina.getInstance().getMessageHandler().broadcastToPlayers(entity.getServer(), withMessage);
        }
    }

    private void handleTickingEntity(Entity entity, Throwable e) {
        if (entity instanceof PlayerEntity player) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                handleTickingPlayer(serverPlayer, e);
            } else {
                handleTickingClient(player, e);
            }
            return;
        }

        Neruina.LOGGER.warn("Neruina caught an exception, see below for cause", e);
        addErrored(entity);
        World world = entity.getWorld();
        if (!world.isClient()) {
            BlockPos pos = entity.getBlockPos();
            TickingEntry tickingEntry = new TickingEntry(entity, true, world.getRegistryKey(), pos, e);
            trackError((Errorable) entity, tickingEntry);
            MessageHandler messageHandler = Neruina.getInstance().getMessageHandler();
            Text message = messageHandler.formatText("neruina.ticking.entity.%s".formatted(
                    Config.getInstance().autoKillTickingEntities
                            ? "killed" : "suspended"
                    ),
                    entity.getName().getString(),
                    messageHandler.posAsNums(pos)
            );
            Text actions = messageHandler.generateResourceActions(tickingEntry);
            if (!Config.getInstance().autoKillTickingEntities) {
                actions = Texter.concatDelimited(
                        Texter.LINE_BREAK,
                        messageHandler.generateEntityActions(entity),
                        actions
                );
            }
            messageHandler.broadcastToPlayers(entity.getServer(), message, actions);
        }
    }

    private void handleTickingPlayer(ServerPlayerEntity player, Throwable e) {
        Neruina.LOGGER.warn("Neruina caught an exception, see below for cause", e);
        MinecraftServer server = player.getServer();
        String name = player.getDisplayName() == null ? player.getName().getString() : player.getDisplayName().getString();
        MessageHandler messageHandler = Neruina.getInstance().getMessageHandler();
        Text message = messageHandler.formatText("neruina.ticking.player", name);
        TickingEntry tickingEntry = new TickingEntry(player, false, player.getWorld().getRegistryKey(), player.getBlockPos(), e);
        trackError(tickingEntry);
        messageHandler.broadcastToPlayers(server, message, messageHandler.generateResourceActions(tickingEntry));
        try {
            player.networkHandler.disconnect(
                    Texter.concat(
                            Texter.translatable("neruina.kick.message"),
                            Texter.translatable("neruina.kick.reason")
                    )
            );
        } catch (NullPointerException ex) {
            Neruina.LOGGER.error("Neruina caught an exception on a player, but the player is not connected, this should not happen. Behaviour is undefined.", ex);
        }
    }

    private void handleTickingClient(PlayerEntity player, Throwable e) {
        if(player.getWorld().isClient() || Platform.isClient()) {
            ClientTickHandler.handleTickingClient(player, e);
        } else {
            Neruina.LOGGER.error("Neruina caught an exception, but the player is not a server player, this should not happen. Behaviour is undefined.", e);
        }
    }

    private void trackError(TickingEntry entry) {
        trackError(null, entry);
    }

    private void trackError(@Nullable Errorable errorable, TickingEntry entry) {
        recentErrors.add(entry);
        addTickingEntry(entry);
        if (errorable != null) {
            errorable.neruina$setTickingEntryId(entry.uuid());
        }
        if (Config.getInstance().tickingExceptionThreshold != -1 && recentErrors.size() >= Config.getInstance().tickingExceptionThreshold) {
            CrashReport report = CrashReport.create(
                    new RuntimeException("Too Many Ticking Exceptions"),
                    "Neruina has caught too many ticking exceptions in a short period of time, something is very wrong, see below for more info"
            );
            CrashReportSection header = report.addElement("Information");
            header.add("Threshold",
                    "%d, set \"ticking_exception_threshold\" to -1 to disable.".formatted(
                            Config.getInstance().tickingExceptionThreshold
                    )
            );
            header.add("Caught", recentErrors.size());
            String wiki = "https://github.com/Bawnorton/Neruina/wiki/Too-Many-Ticking-Exceptions";
            String lines = "=".repeat(wiki.length() + "Wiki".length() + 2);
            header.add("", lines);
            header.add("Wiki", wiki);
            header.add("", lines);
            for (int i = 0; i < recentErrors.size(); i++) {
                TickingEntry error = recentErrors.get(i);
                CrashReportSection section = report.addElement("Ticking Exception #%s - (%s: %s)".formatted(i + 1, error.getCauseType(), error.getCauseName()));
                error.populate(section);
            }
            throw new CrashException(report);
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
}