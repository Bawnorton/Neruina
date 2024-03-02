package com.bawnorton.neruina.handler;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.config.Config;
import com.bawnorton.neruina.exception.TickingException;
import com.bawnorton.neruina.extend.Errorable;
import com.bawnorton.neruina.extend.ErrorableBlockState;
import com.bawnorton.neruina.mixin.accessor.WorldChunkAccessor;
import com.bawnorton.neruina.util.ErroredType;
import com.bawnorton.neruina.util.TickingEntry;
import com.bawnorton.neruina.version.VersionedText;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.toast.SystemToast;
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
            TickingEntry tickingEntry = new TickingEntry(instance, pos, e);
            trackError(tickingEntry);
            messageHandler.broadcastToPlayers(world.getServer(),
                    message,
                    messageHandler.generateHandlingActions(ErroredType.BLOCK_STATE, pos),
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
                TickingEntry tickingEntry = new TickingEntry(blockEntity, pos, e);
                trackError((Errorable) blockEntity, tickingEntry);
                messageHandler.broadcastToPlayers(world.getServer(),
                        message,
                        messageHandler.generateHandlingActions(ErroredType.BLOCK_ENTITY, pos),
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
            TickingEntry tickingEntry = new TickingEntry(instance, player.getBlockPos(), e);
            trackError(tickingEntry, false);
            MessageHandler messageHandler = Neruina.getInstance().getMessageHandler();
            messageHandler.sendToPlayer(player,
                    VersionedText.translatable("neruina.ticking.item_stack", instance.getName().getString(), slot),
                    messageHandler.generateResourceActions(tickingEntry),
                    messageHandler.generateResumeAction(ErroredType.ITEM_STACK, player.getUuidAsString())
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
        entity.kill();
        entity.remove(Entity.RemovalReason.KILLED); // Necessary for any living entity
        removeErrored(entity);
        if (withMessage != null) {
            Neruina.getInstance().getMessageHandler().broadcastToPlayers(entity.getServer(), withMessage);
        }
    }

    private void handleTickingEntity(Entity entity, Throwable e) {
        if (entity instanceof PlayerEntity player) {
            handleTickingPlayer(player, e);
            return;
        }

        Neruina.LOGGER.warn("Neruina caught an exception, see below for cause", e);
        addErrored(entity);
        if (!entity.getWorld().isClient()) {
            BlockPos pos = entity.getBlockPos();
            TickingEntry tickingEntry = new TickingEntry(entity, pos, e);
            trackError((Errorable) entity, tickingEntry);
            MessageHandler messageHandler = Neruina.getInstance().getMessageHandler();
            Text message = messageHandler.formatText("neruina.ticking.entity.%s".formatted(Config.getInstance().autoKillTickingEntities ? "killed" : "suspended"),
                    entity.getName().getString(),
                    messageHandler.posAsNums(pos)
            );
            Text actions = messageHandler.generateResourceActions(tickingEntry);
            if (!Config.getInstance().autoKillTickingEntities) {
                actions = VersionedText.concatDelimited(VersionedText.LINE_BREAK,
                        messageHandler.generateEntityActions(entity),
                        actions
                );
            }
            messageHandler.broadcastToPlayers(entity.getServer(), message, actions);
        }
    }

    private void handleTickingPlayer(PlayerEntity player, Throwable e) {
        Neruina.LOGGER.warn("Neruina caught an exception, see below for cause", e);
        MinecraftServer server = player.getServer();
        if (server == null || !server.isDedicated() && player.getWorld().isClient()) {
            handleTickingClient(player, e);
            return;
        }

        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        String name = player.getDisplayName() == null ? player.getName().getString() : player.getDisplayName().getString();
        MessageHandler messageHandler = Neruina.getInstance().getMessageHandler();
        Text message = messageHandler.formatText("neruina.ticking.player", name);
        TickingEntry tickingEntry = new TickingEntry(player, player.getBlockPos(), e);
        trackError(tickingEntry);
        messageHandler.broadcastToPlayers(server, message, messageHandler.generateResourceActions(tickingEntry));
        serverPlayer.networkHandler.disconnect(VersionedText.concat(VersionedText.translatable("neruina.kick.message"),
                VersionedText.translatable("neruina.kick.reason")
        ));
    }

    private void handleTickingClient(PlayerEntity clientPlayer, Throwable e) {
        Neruina.LOGGER.warn("Neruina caught an exception, see below for cause", e);
        clientPlayer.getWorld().disconnect();
        MinecraftClient client = MinecraftClient.getInstance();
        /*? if >=1.19 {*/
        client.disconnect(new net.minecraft.client.gui.screen.MessageScreen(VersionedText.translatable("menu.savingLevel")));
        /*? } else {*//*
        client.disconnect(new net.minecraft.client.gui.screen.SaveLevelScreen(VersionedText.translatable("menu.savingLevel")));
        *//*? }*/
        client.setScreen(new TitleScreen());
        client.getToastManager().add(SystemToast.create(client,
                SystemToast.Type.WORLD_ACCESS_FAILURE,
                VersionedText.translatable("neruina.toast.title"),
                VersionedText.translatable("neruina.toast.desc")
        ));
    }

    private void trackError(TickingEntry entry) {
        trackError(entry, true);
    }

    private void trackError(TickingEntry entry, boolean persist) {
        trackError(null, entry, persist);
    }

    private void trackError(Errorable errorable, TickingEntry entry) {
        trackError(errorable, entry, true);
    }

    private void trackError(@Nullable Errorable errorable, TickingEntry entry, boolean persist) {
        recentErrors.add(entry);
        if(persist) {
            addTickingEntry(entry);
        }
        if (errorable != null) {
            errorable.neruina$setTickingEntry(entry.uuid());
        }
        if (Config.getInstance().tickingExceptionThreshold != -1 && recentErrors.size() >= Config.getInstance().tickingExceptionThreshold) {
            CrashReport report = CrashReport.create(
                    new RuntimeException("Too Many Ticking Exceptions"),
                    "Neruina has caught too many ticking exceptions in a short period of time, something is very wrong, see below for more info"
            );
            CrashReportSection header = report.addElement("Information");
            header.add("Threshold",
                    "%d, set \"ticking_exception_threshold\" to -1 to disable.".formatted(Config.getInstance().tickingExceptionThreshold)
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
        if (state instanceof ErrorableBlockState errorable) {
            return errorable.neruina$isErrored(pos);
        }
        return false;
    }

    private void addErrored(Object obj) {
        if (obj instanceof Errorable errorable) {
            errorable.neruina$setErrored();
        }
    }

    private void addErrored(BlockState state, BlockPos pos) {
        if (state instanceof ErrorableBlockState errorable) {
            errorable.neruina$setErrored(pos);
        }
    }

    public void removeErrored(Object obj) {
        if (obj instanceof Errorable errorable) {
            errorable.neruina$clearErrored();
            tickingEntries.remove(errorable.neruina$getTickingEntry());
        }
    }

    public void removeErrored(BlockState state, BlockPos pos) {
        if (state instanceof ErrorableBlockState errorable) {
            errorable.neruina$clearErrored(pos);
        }
    }

    public TickingEntry getTickingEntry(UUID uuid) {
        return tickingEntries.get(uuid);
    }

    public Collection<TickingEntry> getTickingEntries() {
        return tickingEntries.values();
    }

    public void addTickingEntry(TickingEntry entry) {
        tickingEntries.put(entry.uuid(), entry);
    }

    public Optional<UUID> getTickingEntryId(Object obj) {
        if (obj instanceof Errorable errorable) {
            return Optional.ofNullable(errorable.neruina$getTickingEntry());
        }
        return Optional.empty();
    }
}
