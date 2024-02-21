package com.bawnorton.neruina.command;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.extend.Errorable;
import com.bawnorton.neruina.extend.ErrorableBlockState;
import com.bawnorton.neruina.report.ReportCode;
import com.bawnorton.neruina.util.TickingEntry;
import com.bawnorton.neruina.version.VersionedText;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import java.util.Collection;

public class NeruinaCommandHandler {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("neruina")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("resume")
                        .then(CommandManager.literal("entity")
                                .then(CommandManager.argument("entity", EntityArgumentType.entity())
                                        .executes(context -> {
                                            try {
                                                Entity entity = EntityArgumentType.getEntity(context, "entity");
                                                if (!((Errorable) entity).neruina$isErrored()) {
                                                    Neruina.MESSAGE_HANDLER.sendFormattedMessage(context.getSource()::sendError, "commands.neruina.resume.entity.not_errored", entity.getName()
                                                            .getString());
                                                    return 0;
                                                }
                                                Neruina.TICK_HANDLER.removeErrored(entity);
                                                Neruina.MESSAGE_HANDLER.sendFeedback(context, "commands.neruina.resume.entity", entity.getName()
                                                        .getString());
                                            } catch (CommandSyntaxException ignored) {
                                            }
                                            return 1;
                                        })))
                        .then(CommandManager.literal("block_entity")
                                .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                        .executes(context -> {
                                            BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
                                            BlockEntity blockEntity = context.getSource()
                                                    .getWorld()
                                                    .getBlockEntity(pos);
                                            if (blockEntity == null) {
                                                Neruina.MESSAGE_HANDLER.sendFormattedMessage(context.getSource()::sendError, "commands.neruina.resume.block_entity.not_found", Neruina.MESSAGE_HANDLER.formatPos(pos));
                                                return 0;
                                            }
                                            World world = context.getSource().getWorld();
                                            WorldChunk worldChunk = world.getWorldChunk(pos);
                                            BlockState state = worldChunk.getBlockState(pos);
                                            Block block = state.getBlock();
                                            String name = block.getName().getString();
                                            if (!((Errorable) blockEntity).neruina$isErrored()) {
                                                Neruina.MESSAGE_HANDLER.sendFormattedMessage(context.getSource()::sendError, "commands.neruina.resume.block_entity.not_errored", name, Neruina.MESSAGE_HANDLER.formatPos(pos));
                                                return 0;
                                            }
                                            Neruina.TICK_HANDLER.removeErrored(blockEntity);
                                            worldChunk.addBlockEntity(blockEntity);
                                            Neruina.MESSAGE_HANDLER.sendFeedback(context, "commands.neruina.resume.block_entity", name, Neruina.MESSAGE_HANDLER.formatPos(pos));
                                            return 1;
                                        })))
                        .then(CommandManager.literal("block_state")
                                .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                        .executes(context -> {
                                            BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
                                            BlockState blockState = context.getSource().getWorld().getBlockState(pos);
                                            String name = blockState.getBlock().getName().getString();
                                            if (!((ErrorableBlockState) blockState).neruina$isErrored(pos)) {
                                                Neruina.MESSAGE_HANDLER.sendFormattedMessage(context.getSource()::sendError, "commands.neruina.resume.block_state.not_errored", name, Neruina.MESSAGE_HANDLER.formatPos(pos));
                                                return 0;
                                            }
                                            Neruina.TICK_HANDLER.removeErrored(blockState, pos);
                                            Neruina.MESSAGE_HANDLER.sendFeedback(context, "commands.neruina.resume.block_state", name, Neruina.MESSAGE_HANDLER.formatPos(pos));
                                            return 1;
                                        }))))
                .then(CommandManager.literal("kill")
                        .then(CommandManager.argument("entity", EntityArgumentType.entities()).executes(context -> {
                            try {
                                Collection<? extends Entity> entities = EntityArgumentType.getEntities(context, "entity");
                                if (entities.size() == 1) {
                                    Entity entity = entities.iterator().next();
                                    String name = entity.getName().getString();
                                    if (!((Errorable) entity).neruina$isErrored()) {
                                        Neruina.MESSAGE_HANDLER.sendFormattedMessage(context.getSource()::sendError, "commands.neruina.kill.not_errored", name);
                                        return 0;
                                    }
                                    Neruina.TICK_HANDLER.killEntity(entity, Neruina.MESSAGE_HANDLER.formatText("commands.neruina.kill", name));
                                } else {
                                    int killed = 0;
                                    for (Entity entity : entities) {
                                        if (!((Errorable) entity).neruina$isErrored()) {
                                            continue;
                                        }
                                        Neruina.TICK_HANDLER.killEntity(entity, null);
                                        killed++;
                                    }
                                    Text message = getKilledResultMessage(entities, killed);
                                    Neruina.MESSAGE_HANDLER.broadcastToPlayers(context.getSource()
                                            .getServer(), message);
                                }
                            } catch (CommandSyntaxException ignored) {
                            }
                            return 1;
                        })))
                .then(CommandManager.literal("report")
                        .then(CommandManager.argument("id", UuidArgumentType.uuid()).executes(context -> {
                            TickingEntry entry = Neruina.TICK_HANDLER.getTickingEntry(UuidArgumentType.getUuid(context, "id"));
                            if (entry == null) {
                                Neruina.MESSAGE_HANDLER.sendFormattedMessage(context.getSource()::sendError, "commands.neruina.report.not_found", UuidArgumentType.getUuid(context, "id"));
                                return 0;
                            }
                            Neruina.AUTO_REPORT_HANDLER.createReports(entry).thenAccept(result -> {
                                ReportCode reportCode = result.getFirst();
                                String url = result.getSecond();
                                switch (reportCode) {
                                    case SUCCESS -> context.getSource()
                                            .sendFeedback(
                                                    /*? if >=1.20 */
                                                    () ->
                                                    VersionedText.format(VersionedText.withStyle(
                                                            VersionedText.translatable("commands.neruina.report.success"),
                                                            style -> style
                                                                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                                                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, VersionedText.translatable("commands.neruina.report.success.tooltip")))
                                                    )), false);
                                    case ALREADY_EXISTS -> Neruina.MESSAGE_HANDLER.sendFormattedMessage(context.getSource()::sendError, "commands.neruina.report.already_exists");
                                    case FAILURE -> Neruina.MESSAGE_HANDLER.sendFormattedMessage(context.getSource()::sendError, "commands.neruina.report.failure");
                                }
                            });
                            return 1;
                        }))));
    }

    private static Text getKilledResultMessage(Collection<? extends Entity> entities, int killed) {
        int missed = entities.size() - killed;
        Text message;
        if (killed == 1 && missed == 1) {
            message = Neruina.MESSAGE_HANDLER.formatText("commands.neruina.kill.multiple.singular_singular");
        } else if (killed == 1) {
            message = Neruina.MESSAGE_HANDLER.formatText("commands.neruina.kill.multiple.singular_plural", missed);
        } else if (missed == 1) {
            message = Neruina.MESSAGE_HANDLER.formatText("commands.neruina.kill.multiple.plural_singular", killed);
        } else {
            message = Neruina.MESSAGE_HANDLER.formatText("commands.neruina.kill.multiple", killed, missed);
        }
        return message;
    }
}