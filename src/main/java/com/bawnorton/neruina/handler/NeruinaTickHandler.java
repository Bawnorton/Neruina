package com.bawnorton.neruina.handler;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.config.Config;
import com.bawnorton.neruina.exception.TickingException;
import com.bawnorton.neruina.extend.Errorable;
import com.bawnorton.neruina.extend.ErrorableBlockState;
import com.bawnorton.neruina.mixin.accessor.WorldChunkAccessor;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/*? if >=1.19 {*/
import net.minecraft.client.gui.screen.MessageScreen;
/*? } else {*//*
import net.minecraft.client.gui.screen.SaveLevelScreen;
*//*? }*/

public final class NeruinaTickHandler {
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
            Text message = Neruina.MESSAGE_HANDLER.formatText("neruina.ticking.block_state",
                    instance.getBlock().getName().getString(),
                    pos.getX(),
                    pos.getY(),
                    pos.getZ()
            );
            Neruina.LOGGER.warn("Neruina Caught An Exception, see below for cause", e);
            addErrored(instance, pos);
            TickingEntry tickingEntry = new TickingEntry(instance, pos, UUID.randomUUID(), e);
            trackError(tickingEntry);
            Neruina.MESSAGE_HANDLER.broadcastToPlayers(world.getServer(),
                    message,
                    Neruina.MESSAGE_HANDLER.generateHandlingActions("block_state", pos),
                    Neruina.MESSAGE_HANDLER.generateResourceActions(tickingEntry)
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
            Text message = Neruina.MESSAGE_HANDLER.formatText("neruina.ticking.block_entity",
                    state.getBlock().getName().getString(),
                    pos.getX(),
                    pos.getY(),
                    pos.getZ()
            );
            Neruina.LOGGER.warn("Neruina caught an exception, see below for cause", e);
            addErrored(blockEntity);
            if (!world.isClient()) {
                TickingEntry tickingEntry = new TickingEntry(blockEntity, pos, UUID.randomUUID(), e);
                trackError(tickingEntry);
                Neruina.MESSAGE_HANDLER.broadcastToPlayers(world.getServer(),
                        message,
                        Neruina.MESSAGE_HANDLER.generateHandlingActions("block_entity", pos),
                        Neruina.MESSAGE_HANDLER.generateResourceActions(tickingEntry)
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
            TickingEntry tickingEntry = new TickingEntry(instance, player.getBlockPos(), UUID.randomUUID(), e);
            trackError(tickingEntry);
            Neruina.MESSAGE_HANDLER.sendToPlayer(player,
                    VersionedText.translatable("neruina.ticking.item_stack", instance.getName().getString(), slot),
                    Neruina.MESSAGE_HANDLER.generateResourceActions(tickingEntry)
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
                killEntity(entity, Neruina.MESSAGE_HANDLER.formatText("neruina.ticking.entity.suspend_failed", entity.getName().getString()));
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
            Neruina.MESSAGE_HANDLER.broadcastToPlayers(entity.getServer(), withMessage);
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
            TickingEntry tickingEntry = new TickingEntry(entity, pos, entity.getUuid(), e);
            trackError(tickingEntry);
            Text message = Neruina.MESSAGE_HANDLER.formatText("neruina.ticking.entity.%s".formatted(Config.getInstance().autoKillTickingEntities ? "killed" : "suspended"),
                    entity.getName().getString(),
                    pos.getX(),
                    pos.getY(),
                    pos.getZ()
            );
            Text actions = Neruina.MESSAGE_HANDLER.generateResourceActions(tickingEntry);
            if (!Config.getInstance().autoKillTickingEntities) {
                actions = VersionedText.concatDelimited(VersionedText.LINE_BREAK,
                        Neruina.MESSAGE_HANDLER.generateEntityActions(entity),
                        actions
                );
            }
            Neruina.MESSAGE_HANDLER.broadcastToPlayers(entity.getServer(), message, actions);
        }
    }

    private void handleTickingPlayer(PlayerEntity player, Throwable e) {
        Neruina.LOGGER.warn("Neruina caught an exception, see below for cause", e);
        MinecraftServer server = player.getServer();
        if (server == null || !server.isDedicated()) {
            handleTickingClient(player, e);
            return;
        }

        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        String name = player.getDisplayName() == null ? player.getName().getString() : player.getDisplayName().getString();
        Text message = Neruina.MESSAGE_HANDLER.formatText("neruina.ticking.player", name);
        TickingEntry tickingEntry = new TickingEntry(player, player.getBlockPos(), UUID.randomUUID(), e);
        trackError(tickingEntry);
        Neruina.MESSAGE_HANDLER.broadcastToPlayers(server, message, Neruina.MESSAGE_HANDLER.generateResourceActions(tickingEntry));
        serverPlayer.networkHandler.disconnect(VersionedText.concat(VersionedText.translatable("neruina.kick.message"),
                VersionedText.translatable("neruina.kick.reason")
        ));
    }

    private void handleTickingClient(PlayerEntity clientPlayer, Throwable e) {
        Neruina.LOGGER.warn("Neruina caught an exception, see below for cause", e);
        clientPlayer.getEntityWorld().disconnect();
        MinecraftClient client = MinecraftClient.getInstance();
        /*? if >=1.19 {*/
        client.disconnect(new MessageScreen(VersionedText.translatable("menu.savingLevel")));
        /*? } else {*//*
        client.disconnect(new SaveLevelScreen(VersionedText.translatable("menu.savingLevel")));
        *//*? }*/
        client.setScreen(new TitleScreen());
        client.getToastManager().add(SystemToast.create(client,
                SystemToast.Type.WORLD_ACCESS_FAILURE,
                VersionedText.translatable("neruina.toast.title"),
                VersionedText.translatable("neruina.toast.desc")
        ));
    }


    private void trackError(TickingEntry e) {
        recentErrors.add(e);
        tickingEntries.put(e.uuid(), e);
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
            header.add("Wiki", "https://github.com/Bawnorton/Neruina/wiki/Too-Many-Ticking-Exceptions");
            for (int i = 0; i < recentErrors.size(); i++) {
                TickingEntry error = recentErrors.get(i);
                CrashReportSection section = report.addElement("Ticking Exception #%s - %s".formatted(i, error.getCauseName()));
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
}
