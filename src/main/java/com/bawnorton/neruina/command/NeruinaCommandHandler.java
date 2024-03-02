package com.bawnorton.neruina.command;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.extend.Errorable;
import com.bawnorton.neruina.extend.ErrorableBlockState;
import com.bawnorton.neruina.handler.MessageHandler;
import com.bawnorton.neruina.report.ReportCode;
import com.bawnorton.neruina.util.ErroredType;
import com.bawnorton.neruina.util.TickingEntry;
import com.bawnorton.neruina.version.VersionedText;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import java.util.Collection;
import java.util.UUID;

public class NeruinaCommandHandler {
    private static final MessageHandler messageHandler = Neruina.getInstance().getMessageHandler();
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("neruina")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("resume")
                        .then(CommandManager.literal("entity")
                                .then(CommandManager.argument("entity", EntityArgumentType.entity())
                                        .executes(NeruinaCommandHandler::executeResumeEntity)
                                )
                        )
                        .then(CommandManager.literal("block_entity")
                                .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                        .executes(NeruinaCommandHandler::executeResumeBlockEntity)
                                )
                        )
                        .then(CommandManager.literal("block_state")
                                .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                        .executes(NeruinaCommandHandler::executeResumeBlockState)
                                )
                        )
                        .then(CommandManager.literal("item_stack")
                                .then(CommandManager.argument("player", EntityArgumentType.entity())
                                        .executes(NeruinaCommandHandler::executeResumeHeldItem)
                                )
                        )
                )
                .then(CommandManager.literal("kill")
                        .then(CommandManager.argument("entity", EntityArgumentType.entities())
                                .executes(NeruinaCommandHandler::executeKill)
                        )
                )
                .then(CommandManager.literal("report")
                        .then(CommandManager.argument("id", UuidArgumentType.uuid())
                                .executes(NeruinaCommandHandler::executeReport)
                        )
                )
                .then(CommandManager.literal("id")
                        .then(CommandManager.argument("entity", EntityArgumentType.entity())
                                .executes(NeruinaCommandHandler::executeIdEntity)
                        )
                        .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                .executes(NeruinaCommandHandler::executeIdBlock)
                        )
                )
                .then(CommandManager.literal("info")
                        .then(CommandManager.argument("id", UuidArgumentType.uuid())
                                .executes(NeruinaCommandHandler::executeInfo)
                        )
                )
        );
    }

    private static int executeResumeEntity(CommandContext<ServerCommandSource> context) {
        try {
            Entity entity = EntityArgumentType.getEntity(context, "entity");
            if (!((Errorable) entity).neruina$isErrored()) {
                messageHandler.sendFormattedMessage(context.getSource()::sendError, "commands.neruina.resume.entity.not_errored", entity.getName()
                        .getString());
                return 0;
            }
            Neruina.getInstance().getTickHandler().removeErrored(entity);
            messageHandler.broadcastToPlayers(context, "commands.neruina.resume.entity", entity.getName()
                    .getString());
        } catch (CommandSyntaxException ignored) {
            messageHandler.sendFormattedMessage(context.getSource()::sendError, "commands.neruina.resume.entity.not_found");
        }
        return 1;
    }

    private static int executeResumeBlockEntity(CommandContext<ServerCommandSource> context) {
        BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
        BlockEntity blockEntity = context.getSource()
                .getWorld()
                .getBlockEntity(pos);
        if (blockEntity == null) {
            messageHandler.sendFormattedMessage(context.getSource()::sendError, "commands.neruina.resume.block_entity.not_found", messageHandler.posAsNums(pos));
            return 0;
        }
        World world = context.getSource().getWorld();
        WorldChunk worldChunk = world.getWorldChunk(pos);
        BlockState state = worldChunk.getBlockState(pos);
        Block block = state.getBlock();
        String name = block.getName().getString();
        if (!((Errorable) blockEntity).neruina$isErrored()) {
            messageHandler.sendFormattedMessage(context.getSource()::sendError, "commands.neruina.resume.block_entity.not_errored", name, messageHandler.posAsNums(pos));
            return 0;
        }
        Neruina.getInstance().getTickHandler().removeErrored(blockEntity);
        worldChunk.addBlockEntity(blockEntity);
        messageHandler.broadcastToPlayers(context, "commands.neruina.resume.block_entity", name, messageHandler.posAsNums(pos));
        return 1;
    }

    private static int executeResumeBlockState(CommandContext<ServerCommandSource> context) {
        BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
        BlockState blockState = context.getSource().getWorld().getBlockState(pos);
        String name = blockState.getBlock().getName().getString();
        if (!((ErrorableBlockState) blockState).neruina$isErrored(pos)) {
            messageHandler.sendFormattedMessage(context.getSource()::sendError, "commands.neruina.resume.block_state.not_errored", name, messageHandler.posAsNums(pos));
            return 0;
        }
        Neruina.getInstance().getTickHandler().removeErrored(blockState, pos);
        messageHandler.broadcastToPlayers(context, "commands.neruina.resume.block_state", name, messageHandler.posAsNums(pos));
        return 1;
    }

    private static int executeResumeHeldItem(CommandContext<ServerCommandSource> context) {
        try {
            PlayerEntity player = EntityArgumentType.getPlayer(context, "player");
            ItemStack stack = player.getStackInHand(player.getActiveHand());
            if(!((Errorable) (Object) stack).neruina$isErrored()) {
                messageHandler.sendFormattedMessage(context.getSource()::sendError, "commands.neruina.resume.item_stack.not_errored", player.getName().getString(), stack.getName().getString());
                return 0;
            }
            Neruina.getInstance().getTickHandler().removeErrored(stack);
            context.getSource().sendFeedback(
                    /*? if >=1.20 */
                    () ->
                    messageHandler.formatText("commands.neruina.resume.item_stack", player.getName().getString(), stack.getName().getString()),
                    false
            );
        } catch (CommandSyntaxException ignored) {
            messageHandler.sendFormattedMessage(context.getSource()::sendError, "commands.neruina.resume.entity.not_found");
        }
        return 1;
    }

    private static int executeKill(CommandContext<ServerCommandSource> context) {
        try {
            Collection<? extends Entity> entities = EntityArgumentType.getEntities(context, "entity");
            if (entities.size() == 1) {
                Entity entity = entities.iterator().next();
                String name = entity.getName().getString();
                if (!((Errorable) entity).neruina$isErrored()) {
                    messageHandler.sendFormattedMessage(context.getSource()::sendError, "commands.neruina.kill.not_errored", name);
                    return 0;
                }
                Neruina.getInstance().getTickHandler().killEntity(entity, messageHandler.formatText("commands.neruina.kill", name));
            } else {
                int killed = 0;
                for (Entity entity : entities) {
                    if (!((Errorable) entity).neruina$isErrored()) {
                        continue;
                    }
                    Neruina.getInstance().getTickHandler().killEntity(entity, null);
                    killed++;
                }

                Text message = getKilledResultMessage(entities, killed);
                messageHandler.broadcastToPlayers(context.getSource()
                        .getServer(), message);
            }
        } catch (CommandSyntaxException ignored) {
            messageHandler.sendFormattedMessage(context.getSource()::sendError, "commands.neruina.kill.not_found");
        }
        return 1;
    }

    private static Text getKilledResultMessage(Collection<? extends Entity> entities, int killed) {
        int missed = entities.size() - killed;
        Text message;
        if (killed == 1 && missed == 1) {
            message = messageHandler.formatText("commands.neruina.kill.multiple.singular_singular");
        } else if (killed == 1) {
            message = messageHandler.formatText("commands.neruina.kill.multiple.singular_plural", missed);
        } else if (missed == 1) {
            message = messageHandler.formatText("commands.neruina.kill.multiple.plural_singular", killed);
        } else {
            message = messageHandler.formatText("commands.neruina.kill.multiple", killed, missed);
        }
        return message;
    }

    private static int executeReport(CommandContext<ServerCommandSource> context) {
        TickingEntry entry = Neruina.getInstance().getTickHandler().getTickingEntry(UuidArgumentType.getUuid(context, "id"));
        if (entry == null) {
            messageHandler.sendFormattedMessage(context.getSource()::sendError, "commands.neruina.report.not_found", UuidArgumentType.getUuid(context, "id"));
            return 0;
        }
        Neruina.getInstance().getAutoReportHandler().createReports(entry).thenAccept(result -> {
            ReportCode reportCode = result.getFirst();
            String url = result.getSecond();
            switch (reportCode) {
                case SUCCESS -> messageHandler.broadcastToPlayers(
                        context.getSource().getServer(),
                        VersionedText.format(VersionedText.withStyle(
                                VersionedText.translatable("commands.neruina.report.success"),
                                style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, VersionedText.translatable("commands.neruina.report.success.tooltip")))
                        ))
                );
                case ALREADY_EXISTS ->
                        messageHandler.sendFormattedMessage(context.getSource()::sendError, "commands.neruina.report.already_exists");
                case FAILURE ->
                        messageHandler.sendFormattedMessage(context.getSource()::sendError, "commands.neruina.report.failure");
            }
        });
        return 1;
    }

    private static int executeIdBlock(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
        BlockEntity blockEntity = context.getSource().getWorld().getBlockEntity(pos);
        Neruina.getInstance().getTickHandler().getTickingEntryId(blockEntity).ifPresentOrElse(uuid -> context.getSource().sendFeedback(
                /*? if >=1.20 */
                () -> 
                VersionedText.withStyle(
                        messageHandler.formatText("commands.neruina.id", uuid),
                        style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid.toString()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, VersionedText.translatable("commands.neruina.id.tooltip")))
                ),
                true
        ), () -> context.getSource().sendError(
                messageHandler.formatText(
                        "commands.neruina.id.block.not_errored",
                        context.getSource().getWorld().getBlockState(pos).getBlock().getName().getString(),
                        messageHandler.posAsNums(pos)
                )
        ));
        return 1;
    }

    private static int executeIdEntity(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Entity entity = EntityArgumentType.getEntity(context, "entity");
        Neruina.getInstance().getTickHandler().getTickingEntryId(entity).ifPresentOrElse(uuid -> context.getSource().sendFeedback(
                /*? if >=1.20 */
                () -> 
                VersionedText.withStyle(
                        messageHandler.formatText("commands.neruina.id", uuid),
                        style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid.toString()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, VersionedText.translatable("commands.neruina.id.tooltip")))
                ),
                true
        ), () -> context.getSource().sendError(
                messageHandler.formatText(
                        "commands.neruina.id.entity.not_errored",
                        entity.getName().getString()
                )
        ));
        return 1;
    }

    private static int executeInfo(CommandContext<ServerCommandSource> context) {
        UUID id = UuidArgumentType.getUuid(context, "id");
        TickingEntry entry = Neruina.getInstance().getTickHandler().getTickingEntry(id);
        if (entry == null) {
            messageHandler.sendFormattedMessage(context.getSource()::sendError, "commands.neruina.info.not_found", id);
            return 0;
        }
        Object cause = entry.getCause();
        if (cause == null) {
            context.getSource().sendFeedback(
                    /*? if >=1.20 */
                    () -> 
                    VersionedText.pad(
                            VersionedText.concatDelimited(
                                    VersionedText.LINE_BREAK,
                                    VersionedText.format(VersionedText.translatable("commands.neruina.info.null_cause", entry.getCauseName(), messageHandler.posAsNums(entry.pos()))),
                                    messageHandler.generateTeleportAction(ErroredType.UNKNOWN, entry.pos()),
                                    messageHandler.generateResourceActions(entry)
                            )
                    ),
                    false
            );
        } else if (cause instanceof Entity entity) {
            context.getSource().sendFeedback(
                    /*? if >=1.20 */
                    () -> 
                    VersionedText.pad(
                            VersionedText.concatDelimited(
                                    VersionedText.LINE_BREAK,
                                    VersionedText.format(VersionedText.translatable("commands.neruina.info.entity", entry.getCauseName(), messageHandler.posAsNums(entry.pos()))),
                                    messageHandler.generateEntityActions(entity),
                                    messageHandler.generateResourceActions(entry)
                            )
                    ),
                    false
            );
        } else if (cause instanceof BlockEntity) {
            context.getSource().sendFeedback(
                    /*? if >=1.20 */
                    () -> 
                    VersionedText.pad(
                            VersionedText.concatDelimited(
                                    VersionedText.LINE_BREAK,
                                    VersionedText.format(VersionedText.translatable("commands.neruina.info.block_entity", entry.getCauseName(), messageHandler.posAsNums(entry.pos()))),
                                    messageHandler.generateHandlingActions(ErroredType.BLOCK_ENTITY, entry.pos()),
                                    messageHandler.generateResourceActions(entry)
                            )
                    ),
                    false
            );
        }
        return 1;
    }
}