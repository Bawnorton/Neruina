package com.bawnorton.neruina.handler;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.config.Config;
import com.bawnorton.neruina.exception.TickingException;
import com.bawnorton.neruina.extend.Errorable;
import com.bawnorton.neruina.extend.ErrorableBlockState;
import com.bawnorton.neruina.mixin.invoker.WorldChunkInvoker;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Consumer;

/*? if >=1.19 {*/
import net.minecraft.client.gui.screen.MessageScreen;
/*? } else {*//*
import net.minecraft.client.gui.screen.SaveLevelScreen;
*//*? }*/

public final class NeruinaTickHandler {
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
                            instance.getBlock()
                                    .getName()
                                    .getString(),
                            pos.getX(),
                            pos.getY(),
                            pos.getZ()
                    )
            );
            Neruina.LOGGER.warn("Neruina Caught An Exception, see below for cause", e);
            addErrored(instance, pos);
            broadcastToPlayers(
                    world.getServer(),
                    VersionedText.concatDelimited(VersionedText.LINE_BREAK, message, generateActions(e))
            );
        }
    }

    public void safelyTickBlockEntity(BlockEntityTicker<? extends BlockEntity> instance, World world, BlockPos pos, BlockState state, BlockEntity blockEntity, Operation<Void> original) {
        try {
            if (isErrored(blockEntity)) {
                if (world.isClient()) return;

                WorldChunk chunk = world.getWorldChunk(pos);
                ((WorldChunkInvoker) chunk).invokeRemoveBlockEntityTicker(pos);
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
                            state.getBlock()
                                 .getName()
                                 .getString(),
                            pos.getX(),
                            pos.getY(),
                            pos.getZ()
                    )
            );
            Neruina.LOGGER.warn("Neruina caught an exception, see below for cause", e);
            addErrored(blockEntity);
            if (!world.isClient()) {
                broadcastToPlayers(
                        world.getServer(),
                        VersionedText.concatDelimited(VersionedText.LINE_BREAK, message, generateActions(e))
                );
            }
        }
    }

    private void handleTickingItemStack(Throwable e, ItemStack instance, boolean world, PlayerEntity player, int slot) {
        if (!Config.getInstance().handleTickingItemStacks) {
            throw TickingException.notHandled("handle_ticking_item_stacks", e);
        }
        Neruina.LOGGER.warn("Neruina caught an exception, see below for cause", e);
        addErrored(instance);
        if (world) {
            player.sendMessage(
                    VersionedText.concatDelimited(
                            VersionedText.LINE_BREAK,
                            VersionedText.format(
                                    VersionedText.translatableWithFallback(
                                            "neruina.ticking.item_stack",
                                            "Caught Ticking Item Stack [%s] in slot [%s]. See your logs for more info.",
                                            instance.getItem()
                                                    .getName()
                                                    .getString(),
                                            slot
                                    )
                            ),
                            generateActions(e)
                    ),
                    false
            );
        }
    }

    private void handleErroredEntity(Entity entity) {
        try {
            if (entity instanceof PlayerEntity) return;
            if (entity.getWorld().isClient()) return;

            entity.kill();
            entity.remove(Entity.RemovalReason.KILLED);
            entity.baseTick();
            removeErrored(entity);
        } catch (Throwable e) {
            throw new TickingException("Exception occurred while handling errored entity", e);
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
            Vec3d pos = entity.getPos();
            Text message = VersionedText.format(
                    VersionedText.translatableWithFallback(
                            "neruina.ticking.entity",
                            "Caught Ticking Entity [%s] at position [x=%s, y=%s, z=%s]. It has been killed, see your logs for more info.",
                            entity.getName()
                                  .getString(),
                            Math.floor(pos.x),
                            Math.floor(pos.y),
                            Math.floor(pos.z)
                    )
            );
            broadcastToPlayers(
                    entity.getServer(),
                    VersionedText.concatDelimited(VersionedText.LINE_BREAK, message, generateActions(e))
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
        String name = player.getDisplayName() == null ? player.getName().getString() : player.getDisplayName()
                                                                                             .getString();
        Text message = VersionedText.format(VersionedText.translatableWithFallback(
                "neruina.ticking.player",
                "Caught Ticking Player, %s has been kicked. See your logs for more info.",
                name
        ));
        broadcastToPlayers(
                server,
                VersionedText.concatDelimited(VersionedText.LINE_BREAK, message, generateActions(e))
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

    private void broadcastToPlayers(MinecraftServer server, Text message) {
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
}
