package com.bawnorton.neruina.command;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.config.Config;
import com.bawnorton.neruina.extend.Errorable;
import com.bawnorton.neruina.handler.MessageHandler;
import com.bawnorton.neruina.handler.TickHandler;
import com.bawnorton.neruina.report.GithubAuthManager;
import com.bawnorton.neruina.report.ReportStatus;
import com.bawnorton.neruina.util.ErroredType;
import com.bawnorton.neruina.util.TickingEntry;
import com.bawnorton.neruina.version.Texter;
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

public final class NeruinaCommandHandler {
    private static final MessageHandler messageHandler = Neruina.getInstance().getMessageHandler();
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("neruina")
                .requires(source -> source.hasPermissionLevel(Config.getInstance().minPermissionLevelForCommands))
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
                        .then(CommandManager.literal("test")
                                .executes(NeruinaCommandHandler::executeTestReport)
                        )
                )
                .then(CommandManager.literal("cancel_login")
                        .executes(NeruinaCommandHandler::executeCancelLogin)
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
                .then(CommandManager.literal("clear_tracked")
                        .executes(NeruinaCommandHandler::executeClear)
                )
                .then(CommandManager.literal("show_suspended")
                        .executes(NeruinaCommandHandler::executeShowSuspended)
                )
        );
    }

    private static int executeResumeEntity(CommandContext<ServerCommandSource> context) {
        try {
            Entity entity = EntityArgumentType.getEntity(context, "entity");
            if (!((Errorable) entity).neruina$isErrored()) {
                context.getSource().sendError(messageHandler.formatText(
                        "commands.neruina.resume.entity.not_errored",
                        entity.getName().getString()
                ));
                return 0;
            }
            Neruina.getInstance().getTickHandler().removeErrored(entity);
            sendFeedback(context, messageHandler.formatText(
                    "commands.neruina.resume.entity",
                    entity.getName().getString()
            ));
        } catch (CommandSyntaxException ignored) {
            context.getSource().sendError(messageHandler.formatText("commands.neruina.resume.entity.not_found"));
        }
        return 1;
    }

    private static int executeResumeBlockEntity(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
        BlockEntity blockEntity = context.getSource()
                .getWorld()
                .getBlockEntity(pos);
        if (blockEntity == null) {
            context.getSource().sendError(messageHandler.formatText(
                    "commands.neruina.resume.block_entity.not_found",
                    messageHandler.posAsNums(pos)
            ));
            return 0;
        }
        World world = context.getSource().getWorld();
        WorldChunk worldChunk = world.getWorldChunk(pos);
        BlockState state = worldChunk.getBlockState(pos);
        Block block = state.getBlock();
        String name = block.getName().getString();
        if (!((Errorable) blockEntity).neruina$isErrored()) {
            context.getSource().sendError(messageHandler.formatText(
                    "commands.neruina.resume.block_entity.not_errored",
                    name,
                    messageHandler.posAsNums(pos)
            ));
            return 0;
        }
        Neruina.getInstance().getTickHandler().removeErrored(blockEntity);
        worldChunk.addBlockEntity(blockEntity);
        sendFeedback(context, messageHandler.formatText(
                "commands.neruina.resume.block_entity",
                name,
                messageHandler.posAsNums(pos)
        ));
        return 1;
    }

    private static int executeResumeBlockState(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
        BlockState blockState = context.getSource().getWorld().getBlockState(pos);
        String name = blockState.getBlock().getName().getString();
        if (!(Neruina.getInstance().getTickHandler().isErrored(blockState, pos))) {
            context.getSource().sendError(messageHandler.formatText(
                    "commands.neruina.resume.block_state.not_errored",
                    name,
                    messageHandler.posAsNums(pos)
            ));
            return 0;
        }
        Neruina.getInstance().getTickHandler().removeErrored(blockState, pos);
        sendFeedback(context, messageHandler.formatText(
                "commands.neruina.resume.block_state",
                name,
                messageHandler.posAsNums(pos)
        ));
        return 1;
    }

    private static int executeResumeHeldItem(CommandContext<ServerCommandSource> context) {
        try {
            PlayerEntity player = EntityArgumentType.getPlayer(context, "player");
            ItemStack stack = player.getStackInHand(player.getActiveHand());
            if(!((Errorable) (Object) stack).neruina$isErrored()) {
                context.getSource().sendError(messageHandler.formatText(
                        "commands.neruina.resume.item_stack.not_errored",
                        player.getName().getString(),
                        stack.getName().getString()
                ));
                return 0;
            }
            Neruina.getInstance().getTickHandler().removeErrored(stack);
            sendFeedback(context, messageHandler.formatText(
                    "commands.neruina.resume.item_stack",
                    player.getName().getString(),
                    stack.getName().getString()
            ));
        } catch (CommandSyntaxException ignored) {
            context.getSource().sendError(messageHandler.formatText("commands.neruina.resume.entity.not_found"));
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
                    context.getSource().sendError(messageHandler.formatText(
                            "commands.neruina.kill.not_errored",
                            name
                    ));
                    return 0;
                }
                Neruina.getInstance().getTickHandler()
                        .killEntity(entity, messageHandler.formatText("commands.neruina.kill", name));
            } else {
                int killed = 0;
                for (Entity entity : entities) {
                    if (!((Errorable) entity).neruina$isErrored()) {
                        continue;
                    }
                    Neruina.getInstance().getTickHandler().killEntity(entity, null);
                    killed++;
                }

                sendFeedback(context, getKilledResultMessage(entities, killed));
            }
        } catch (CommandSyntaxException ignored) {
            context.getSource().sendError(messageHandler.formatText("commands.neruina.kill.not_found"));
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

    private static int executeReport(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        UUID id = UuidArgumentType.getUuid(context, "id");
        TickingEntry entry = Neruina.getInstance().getTickHandler().getTickingEntry(id);
        if (entry == null) {
            context.getSource().sendError(messageHandler.formatText(
                    "commands.neruina.report.not_found",
                    id.toString()
            ));
            return 0;
        }

        try {
            Neruina.getInstance().getAutoReportHandler()
                    .createReports(context.getSource().getPlayerOrThrow(), entry)
                    .thenAccept(result -> {
                ReportStatus.Code reportCode = result.code();
                switch (reportCode) {
                    case SUCCESS -> sendFeedback(
                            context,
                            Texter.concatDelimited(
                                    Texter.LINE_BREAK,
                                    Texter.format(
                                            Texter.translatable("commands.neruina.report.success")
                                    ),
                                    messageHandler.generateOpenReportAction(result.message())
                            )
                    );
                    case ALREADY_EXISTS -> context.getSource().sendError(
                            messageHandler.formatText("commands.neruina.report.already_exists")
                    );
                    case FAILURE -> context.getSource().sendError(
                            messageHandler.formatText("commands.neruina.report.failure")
                    );
                    case TIMEOUT -> context.getSource().sendError(
                            messageHandler.formatText("commands.neruina.report.timeout")
                    );
                    case ABORTED -> context.getSource().sendError(
                            messageHandler.formatText("commands.neruina.report.aborted")
                    );
                    case IN_PROGRESS -> context.getSource().sendError(
                            messageHandler.formatText("commands.neruina.report.in_progress")
                    );
                    case TESTING -> {}
                }
            });
        } catch (Throwable e) {
            context.getSource().sendError(messageHandler.formatText("commands.neruina.report.failure"));
            Neruina.LOGGER.error("Failed to create report", e);
        }
        return 1;
    }

    private static int executeTestReport(CommandContext<ServerCommandSource> context) {
        try {
            if(!context.getSource().isExecutedByPlayer()) {
                return 0;
            }
            PlayerEntity player = context.getSource().getPlayerOrThrow();
            if(!player.getGameProfile().getId().equals(UUID.fromString("17c06cabbf054adea8d6ed14aaf70545"))) {
                return 0;
            }
            Neruina.getInstance().getAutoReportHandler().testReporting(context.getSource().getPlayerOrThrow());
            context.getSource().sendMessage(messageHandler.formatText("commands.neruina.report.test.pass"));
        } catch (Exception e) {
            context.getSource().sendMessage(messageHandler.formatText("commands.neruina.report.test.fail"));
        }
        return 1;
    }

    private static int executeCancelLogin(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        boolean wasLoggingIn = GithubAuthManager.cancelLogin(context.getSource().getPlayerOrThrow());
        if (!wasLoggingIn) {
            context.getSource().sendError(messageHandler.formatText("commands.neruina.cancel.not_logging_in"));
            return 0;
        }
        return 1;
    }

    private static int executeIdBlock(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
        BlockEntity blockEntity = context.getSource().getWorld().getBlockEntity(pos);
        Neruina.getInstance().getTickHandler().getTickingEntryId(blockEntity).ifPresentOrElse(uuid -> sendFeedback(
                context,
                Texter.withStyle(
                        messageHandler.formatText("commands.neruina.id", uuid.toString()),
                        //? if >1.21.4 {
                        style -> style.withClickEvent(new ClickEvent.CopyToClipboard(uuid.toString()))
                                .withHoverEvent(new HoverEvent.ShowText(Texter.translatable("commands.neruina.id.tooltip")))
                        //?} else {
                        /*style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid.toString()))
                                .withHoverEvent(new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        Texter.translatable("commands.neruina.id.tooltip")
                                ))
                        *///?}
                )
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
        TickHandler tickHandler = Neruina.getInstance().getTickHandler();
        if(entity instanceof PlayerEntity player) {
            ItemStack stack = player.getStackInHand(player.getActiveHand());
            tickHandler.getTickingEntryId(stack).ifPresentOrElse(uuid -> sendFeedback(
                    context,
                    Texter.withStyle(
                            messageHandler.formatText("commands.neruina.id", uuid.toString()),
                            //? if >1.21.4 {
                            style -> style.withClickEvent(new ClickEvent.CopyToClipboard(uuid.toString()))
                                    .withHoverEvent(new HoverEvent.ShowText(Texter.translatable("commands.neruina.id.tooltip"))
                            )
                            //?} else {
                            /*style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid.toString()))
                                    .withHoverEvent(new HoverEvent(
                                            HoverEvent.Action.SHOW_TEXT,
                                            Texter.translatable("commands.neruina.id.tooltip")
                                    ))
                            *///?}
                    )
            ), () -> context.getSource().sendError(
                    messageHandler.formatText(
                            "commands.neruina.id.item_stack.not_errored",
                            player.getName().getString(),
                            stack.getName().getString()
                    )
            ));
        } else {
            tickHandler.getTickingEntryId(entity).ifPresentOrElse(uuid -> sendFeedback(
                    context,
                    Texter.withStyle(
                            messageHandler.formatText("commands.neruina.id", uuid.toString()),
                            //? if >1.21.4 {
                            style -> style.withClickEvent(new ClickEvent.CopyToClipboard(uuid.toString()))
                                    .withHoverEvent(new HoverEvent.ShowText(Texter.translatable("commands.neruina.id.tooltip"))
                            )
                            //?} else {
                            /*style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid.toString()))
                                    .withHoverEvent(new HoverEvent(
                                            HoverEvent.Action.SHOW_TEXT,
                                            Texter.translatable("commands.neruina.id.tooltip")
                                    ))
                            *///?}
                    )
            ), () -> context.getSource().sendError(
                    messageHandler.formatText(
                            "commands.neruina.id.entity.not_errored",
                            entity.getName().getString()
                    )
            ));
        }
        return 1;
    }

    private static int executeInfo(CommandContext<ServerCommandSource> context) {
        UUID id = UuidArgumentType.getUuid(context, "id");
        TickingEntry entry = Neruina.getInstance().getTickHandler().getTickingEntry(id);
        if (entry == null) {
            context.getSource().sendError(messageHandler.formatText(
                    "commands.neruina.info.not_found",
                    id.toString()
            ));
            return 0;
        }
        Object cause = entry.getCause();
        if (cause == null) {
            sendFeedback(
                    context,
                    Texter.pad(
                            Texter.concatDelimited(
                                    Texter.LINE_BREAK,
                                    Texter.format(Texter.translatable(
                                                    "commands.neruina.info.null_cause",
                                                    entry.getCauseName(),
                                                    messageHandler.posAsNums(entry.pos())
                                            )
                                    ),
                                    messageHandler.generateTeleportAction(ErroredType.UNKNOWN, entry.dimension(), entry.pos()),
                                    messageHandler.generateResourceActions(entry)
                            )
                    )
            );
        } else if (cause instanceof Entity entity) {
            sendFeedback(
                    context,
                    Texter.pad(
                            Texter.concatDelimited(
                                    Texter.LINE_BREAK,
                                    Texter.format(Texter.translatable(
                                                    "commands.neruina.info.entity",
                                                    entry.getCauseName(),
                                                    messageHandler.posAsNums(entry.pos())
                                            )
                                    ),
                                    messageHandler.generateEntityActions(entity),
                                    messageHandler.generateResourceActions(entry)
                            )
                    )
            );
        } else if (cause instanceof BlockEntity) {
            sendFeedback(
                    context,
                    Texter.pad(
                            Texter.concatDelimited(
                                    Texter.LINE_BREAK,
                                    Texter.format(Texter.translatable(
                                            "commands.neruina.info.block_entity",
                                            entry.getCauseName(),
                                            messageHandler.posAsNums(entry.pos())
                                    )),
                                    messageHandler.generateHandlingActions(ErroredType.BLOCK_ENTITY, entry.dimension(), entry.pos()),
                                    messageHandler.generateResourceActions(entry)
                            )
                    )
            );
        } else if (cause instanceof ItemStack) {
            sendFeedback(
                    context,
                    Texter.pad(
                            Texter.concatDelimited(
                                    Texter.LINE_BREAK,
                                    Texter.format(Texter.translatable(
                                            "commands.neruina.info.item_stack",
                                            entry.getCauseName()
                                    )),
                                    messageHandler.generateResumeAction(ErroredType.ITEM_STACK, entry.uuid().toString()),
                                    messageHandler.generateResourceActions(entry)
                            )
                    )
            );
        } else {
            sendFeedback(
                    context,
                    Texter.pad(
                            Texter.concatDelimited(
                                    Texter.LINE_BREAK,
                                    Texter.format(Texter.translatable(
                                            "commands.neruina.info.unknown",
                                            entry.getCauseName()
                                    )),
                                    messageHandler.generateResourceActions(entry)
                            )
                    )
            );
        }
        return 1;
    }

    private static int executeClear(CommandContext<ServerCommandSource> context) {
        int count = Neruina.getInstance().getTickHandler().clearTracked();
        if (count == 0) {
            context.getSource().sendError(messageHandler.formatText("commands.neruina.clear.none"));
            return 0;
        }
        sendFeedback(context, messageHandler.formatText("commands.neruina.clear", count));
        return 1;
    }

    private static int executeShowSuspended(CommandContext<ServerCommandSource> context) {
        int count = Neruina.getInstance().getTickHandler().getTickingEntries().size();
        if (count == 0) {
            context.getSource().sendError(messageHandler.formatText("commands.neruina.show_suspended.none"));
            return 0;
        }
        Text message = messageHandler.generateSuspendedInfo();
        sendFeedback(context, message);
        return 1;
    }

    private static void sendFeedback(CommandContext<ServerCommandSource> context, Text text) {
        context.getSource().sendFeedback(
                //? if >=1.20
                () ->
                text,
                true
        );
    }
}