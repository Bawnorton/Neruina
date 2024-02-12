package com.bawnorton.neruina.handler;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.config.Config;
import com.bawnorton.neruina.exception.TickingException;
import com.bawnorton.neruina.extend.CrashReportSectionExtender;
import com.bawnorton.neruina.extend.Errorable;
import com.bawnorton.neruina.extend.ErrorableBlockState;
import com.bawnorton.neruina.mixin.accessor.WorldChunkAccessor;
import com.bawnorton.neruina.thread.ConditionalRunnable;
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
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;

/*? if >=1.19 {*/
import net.minecraft.client.gui.screen.MessageScreen;
/*? } else {*//*
import net.minecraft.client.gui.screen.SaveLevelScreen;
*//*? }*/

public final class NeruinaTickHandler {
    private final List<Errored> recentErrors = new ArrayList<>();
    private int stopwatch = 0;

    public void tick() {
        stopwatch++;
        if(stopwatch >= 200) {
            if(!recentErrors.isEmpty()) recentErrors.remove(0);
            stopwatch = 0;
        }
    }

    public void safelyTickItemStack(ItemStack instance, World world, Entity entity, int slot, boolean selected, Operation<Void> original) {
        try {
            if (isErrored(instance)) return;
            original.call(instance, world, entity, slot, selected);
        } catch (Throwable e) {
            handleTickingItemStack(e, instance, !world.isClient(), (PlayerEntity) entity, slot);
        }
    }

    public void safelyTickItemStack(ItemStack instance, World world, PlayerEntity player, int slot, int selected, Operation<Void> original) {
        try {
            if (isErrored(instance)) return;
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
            if (isErrored(instance, pos)) return;
            original.call(instance, world, pos, random);
        } catch (Throwable e) {
            if (!Config.getInstance().handleTickingBlockStates) {
                throw TickingException.notHandled("handle_ticking_block_states", e);
            }
            Text message = VersionedText.format(
                    VersionedText.translatableWithFallback(
                            "neruina.ticking.block_state",
                            "Caught Ticking Block from random tick [%s] at position [x=%s, y=%s, z=%s]. See your logs for more info.",
                            instance.getBlock().getName().getString(),
                            pos.getX(),
                            pos.getY(),
                            pos.getZ()
                    )
            );
            Neruina.LOGGER.warn("Neruina Caught An Exception, see below for cause", e);
            addErrored(instance, pos);
            trackError(new Errored(instance, pos, e));
            broadcastToPlayers(
                    world.getServer(),
                    VersionedText.pad(VersionedText.concatDelimited(VersionedText.LINE_BREAK, message, generateActions(e)))
            );
        }
    }

    public void safelyTickBlockEntity(BlockEntityTicker<? extends BlockEntity> instance, World world, BlockPos pos, BlockState state, BlockEntity blockEntity, Operation<Void> original) {
        try {
            if (isErrored(blockEntity)) {
                if (world.isClient()) return;

                WorldChunk chunk = world.getWorldChunk(pos);
                ((WorldChunkAccessor) chunk).invokeRemoveBlockEntityTicker(pos);
                return;
            }
            original.call(instance, world, pos, state, blockEntity);
        } catch (Throwable e) {
            if (!Config.getInstance().handleTickingBlockEntities) {
                throw TickingException.notHandled("handle_ticking_block_entities", e);
            }
            Text message = VersionedText.format(
                    VersionedText.translatableWithFallback(
                            "neruina.ticking.block_entity",
                            "Caught Ticking Block Entity [%s] at position [x=%s, y=%s, z=%s]. See your logs for more info.",
                            state.getBlock().getName().getString(),
                            pos.getX(),
                            pos.getY(),
                            pos.getZ()
                    )
            );
            Neruina.LOGGER.warn("Neruina caught an exception, see below for cause", e);
            addErrored(blockEntity);
            if (!world.isClient()) {
                trackError(new Errored(blockEntity, pos, e));
                broadcastToPlayers(
                        world.getServer(),
                        VersionedText.pad(VersionedText.concatDelimited(VersionedText.LINE_BREAK, message, generateActions(e)))
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
            trackError(new Errored(instance, player.getBlockPos(), e));
            player.sendMessage(
                    VersionedText.pad(
                            VersionedText.concatDelimited(
                                VersionedText.LINE_BREAK,
                                VersionedText.format(
                                        VersionedText.translatableWithFallback(
                                                "neruina.ticking.item_stack",
                                                "Caught Ticking Item Stack [%s] in slot [%s]. See your logs for more info.",
                                                instance.getItem().getName().getString(),
                                                slot
                                        )
                                ),
                                generateActions(e)
                        )
                    ),
                    false
            );
        }
    }

    private void handleErroredEntity(Entity entity) {
        try {
            if (entity instanceof PlayerEntity) return;
            if (entity.getWorld().isClient()) return;

            entity.baseTick();
            if(Config.getInstance().autoKillTickingEntities) {
                killEntity(entity, null);
            }
        } catch (Throwable e) {
            try {
                killEntity(entity, VersionedText.translatableWithFallback(
                        "neruina.ticking.entity.suspend_failed",
                        "Could not suspend entity [%s], killing it instead.",
                        entity.getName().getString()
                ));
            } catch (Throwable ex) {
                throw new TickingException("Exception occurred while handling errored entity", ex);
            }
        }
    }

    public void killEntity(Entity entity, @Nullable Text withMessage) {
        entity.kill();
        entity.remove(Entity.RemovalReason.KILLED);
        removeErrored(entity);
        if(withMessage != null) broadcastToPlayers(entity.getServer(), VersionedText.format(withMessage));
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
            trackError(new Errored(entity, pos, e));
            Text message = VersionedText.format(
                    VersionedText.translatableWithFallback(
                            "neruina.ticking.entity",
                            "Caught Ticking Entity [%s] at position [x=%s, y=%s, z=%s]. It has been %s, see your logs for more info.",
                            entity.getName().getString(),
                            pos.getX(),
                            pos.getY(),
                            pos.getZ(),
                            Config.getInstance().autoKillTickingEntities ? "killed" : "suspended"
                    )
            );
            Text actions = generateActions(e);
            if(!Config.getInstance().autoKillTickingEntities) {
                actions = VersionedText.concatDelimited(VersionedText.LINE_BREAK, generateEntityActions(entity), actions);
            }
            broadcastToPlayers(
                    entity.getServer(),
                    VersionedText.pad(VersionedText.concatDelimited(VersionedText.LINE_BREAK, message, actions))
            );
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
        Text message = VersionedText.format(VersionedText.translatableWithFallback(
                "neruina.ticking.player",
                "Caught Ticking Player, %s has been kicked. See your logs for more info.",
                name
        ));
        broadcastToPlayers(
                server,
                VersionedText.pad(VersionedText.concatDelimited(VersionedText.LINE_BREAK, message, generateActions(e)))
        );
        serverPlayer.networkHandler.disconnect(
                VersionedText.concat(
                        VersionedText.translatableWithFallback(
                                "neruina.kick.message",
                                "You have been kicked due to a ticking exception on your player. Server logs will have more info."
                        ),
                        VersionedText.translatableWithFallback(
                                "neruina.kick.reason",
                                "\n\nWhy was I kicked?\n\nWhen a ticking exception occurs on a normal entity Neruina will kill it to prevent the server from crashing, but if that entity happens to be a player, it will kick them instead. If you weren't kicked the server would have crashed."
                        )
                )
        );
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
        client.getToastManager().add(SystemToast.create(
                client,
                SystemToast.Type.WORLD_ACCESS_FAILURE,
                VersionedText.translatableWithFallback("neruina.toast.title", "Ticking Exception"),
                VersionedText.translatableWithFallback(
                        "neruina.toast.desc",
                        "Neruina caught a ticking execption on your client, see your logs for more info."
                )
        ));
    }

    public void broadcastToPlayers(MinecraftServer server, Text message) {
        ConditionalRunnable.create(() -> {
            switch (Config.getInstance().logLevel) {
                case DISABLED -> {
                }
                /*? if >=1.19 {*/
                case EVERYONE -> server.getPlayerManager().broadcast(message, false);
                /*? } else {*//*
                case EVERYONE -> server.getPlayerManager()
                                       .getPlayerList()
                                       .forEach(player -> player.sendMessage(message, false));
                *//*? }*/
                case OPERATORS -> server.getPlayerManager()
                        .getPlayerList()
                        .stream()
                        .filter(player -> server.getPermissionLevel(player.getGameProfile()) >= server.getOpPermissionLevel())
                        .forEach(player -> player.sendMessage(message, false));
            }
        }, () -> server.getPlayerManager().getCurrentPlayerCount() > 0);
    }

    private Text generateActions(Throwable e) {
        StringWriter traceString = new StringWriter();
        PrintWriter writer = new PrintWriter(traceString);
        e.printStackTrace(writer);
        String trace = traceString.toString();
        writer.flush();
        writer.close();
        return VersionedText.concatDelimited(
                VersionedText.SPACE,
                Texts.bracketed(
                        VersionedText.withStyle(
                                VersionedText.translatableWithFallback("neruina.info", "What Is This?"),
                                style -> style.withColor(Formatting.GREEN)
                                        .withClickEvent(new ClickEvent(
                                                ClickEvent.Action.OPEN_URL,
                                                "https://github.com/Bawnorton/Neruina/wiki/What-Is-This%3F"
                                        ))
                                        .withHoverEvent(new HoverEvent(
                                                HoverEvent.Action.SHOW_TEXT,
                                                VersionedText.translatableWithFallback(
                                                        "neruina.info.tooltip",
                                                        "Click here to learn more about ticking exceptions and what Neruina does"
                                                )
                                        ))
                        )
                ),
                Texts.bracketed(
                        VersionedText.withStyle(
                                VersionedText.translatableWithFallback("neruina.copy_crash", "Copy Crash"),
                                style -> style.withColor(Formatting.GOLD)
                                        .withClickEvent(new ClickEvent(
                                                ClickEvent.Action.COPY_TO_CLIPBOARD,
                                                trace
                                        ))
                                        .withHoverEvent(new HoverEvent(
                                                HoverEvent.Action.SHOW_TEXT,
                                                VersionedText.translatableWithFallback(
                                                        "neruina.copy_crash.tooltip",
                                                        "Copies the caught ticking exception to your clipboard"
                                                )
                                        ))
                        )
                )
        );
    }

    private Text generateEntityActions(Entity entity) {
        return VersionedText.concatDelimited(
                VersionedText.SPACE,
                Texts.bracketed(
                        VersionedText.withStyle(
                                VersionedText.translatableWithFallback("neruina.teleport", "Teleport"),
                                style -> style.withColor(Formatting.DARK_AQUA)
                                        .withClickEvent(new ClickEvent(
                                                ClickEvent.Action.RUN_COMMAND,
                                                "/tp @s " + entity.getBlockPos().getX() + " " + entity.getBlockPos().getY() + " " + entity.getBlockPos().getZ()
                                        ))
                                        .withHoverEvent(new HoverEvent(
                                                HoverEvent.Action.SHOW_TEXT,
                                                VersionedText.translatableWithFallback(
                                                        "neruina.teleport.tooltip",
                                                        "Teleports you to the position of the ticking entity"
                                                )
                                        ))
                        )
                ),
                Texts.bracketed(
                        VersionedText.withStyle(
                                VersionedText.translatableWithFallback("neruina.kill_entity", "Kill Entity"),
                                style -> style.withColor(Formatting.DARK_RED)
                                        .withClickEvent(new ClickEvent(
                                                ClickEvent.Action.RUN_COMMAND,
                                                "/neruina kill " + entity.getUuid()
                                        ))
                                        .withHoverEvent(new HoverEvent(
                                                HoverEvent.Action.SHOW_TEXT,
                                                VersionedText.translatableWithFallback(
                                                        "neruina.kill_entity.tooltip",
                                                        "Kills the entity that caused the ticking exception"
                                                )
                                        ))
                        )
                ),
                Texts.bracketed(
                        VersionedText.withStyle(
                                VersionedText.translatableWithFallback("neruina.try_resume", "Try Resume"),
                                style -> style.withColor(Formatting.YELLOW)
                                        .withClickEvent(new ClickEvent(
                                                ClickEvent.Action.RUN_COMMAND,
                                                "/neruina resume " + entity.getUuid()
                                        ))
                                        .withHoverEvent(new HoverEvent(
                                                HoverEvent.Action.SHOW_TEXT,
                                                VersionedText.translatableWithFallback(
                                                        "neruina.try_resume.tooltip",
                                                        "Allows the entity to resume ticking. If it crashes again it will be resuspended."
                                                )
                                        ))
                        )
                )
        );
    }

    private void trackError(Errored e) {
        recentErrors.add(e);
        if(Config.getInstance().tickingExceptionThreshold != -1 && recentErrors.size() >= Config.getInstance().tickingExceptionThreshold) {
            CrashReport report = CrashReport.create(new RuntimeException("Too Many Ticking Exceptions"), "Neruina has caught too many ticking exceptions in a short period of time, something is very wrong, see below for more info");
            CrashReportSection header = report.addElement("Information");
            header.add("Threshold", Config.getInstance().tickingExceptionThreshold + ", set \"ticking_exception_threshold\" to -1 to disable.");
            header.add("Caught", recentErrors.size());
            header.add("Wiki", "https://github.com/Bawnorton/Neruina/wiki/Too-Many-Ticking-Exceptions");
            for(int i = 0; i < recentErrors.size(); i++) {
                Errored error = recentErrors.get(i);
                CrashReportSection section = report.addElement("Ticking Exception #%s - %s".formatted(i, error.name()));
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

    private record Errored(Object obj, BlockPos pos, Throwable e) {
        public String name() {
            return obj.getClass().getSimpleName();
        }

        public void populate(CrashReportSection section) {
            ((CrashReportSectionExtender) section).neruin$setStacktrace(e);
            if(obj instanceof Entity entity) {
                entity.populateCrashReport(section);
            } else if (obj instanceof BlockEntity blockEntity) {
                blockEntity.populateCrashReport(section);
            } else if (obj instanceof BlockState state) {
                section.add("Position", pos);
                section.add("BlockState", state);
            } else if (obj instanceof ItemStack stack) {
                section.add("ItemStack", stack);
            } else {
                section.add("Errored", obj);
            }
        }
    }
}
