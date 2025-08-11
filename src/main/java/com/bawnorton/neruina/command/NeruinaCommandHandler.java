package com.bawnorton.neruina.command;

import com.bawnorton.configurable.api.ConfigurableApi;
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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import java.util.Collection;
import java.util.UUID;

public final class NeruinaCommandHandler {
    private static final MessageHandler messageHandler = Neruina.getInstance().getMessageHandler();
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("neruina")
                .requires(source -> source.hasPermission(Config.minPermissionLevelForCommands))
                .then(Commands.literal("reload")
                        .executes(context -> {
                            ConfigurableApi.loadFromDisk();
                            sendSuccess(context, messageHandler.formatText("commands.neruina.reload"));
                            return 1;
                        })
                )
                .then(Commands.literal("resume")
                        .then(Commands.literal("entity")
                                .then(Commands.argument("entity", EntityArgument.entity())
                                        .executes(NeruinaCommandHandler::executeResumeEntity)
                                )
                        )
                        .then(Commands.literal("block_entity")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(NeruinaCommandHandler::executeResumeBlockEntity)
                                )
                        )
                        .then(Commands.literal("block_state")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(NeruinaCommandHandler::executeResumeBlockState)
                                )
                        )
                        .then(Commands.literal("item_stack")
                                .then(Commands.argument("player", EntityArgument.entity())
                                        .executes(NeruinaCommandHandler::executeResumeHeldItem)
                                )
                        )
                )
                .then(Commands.literal("kill")
                        .then(Commands.argument("entity", EntityArgument.entities())
                                .executes(NeruinaCommandHandler::executeKill)
                        )
                )
                .then(Commands.literal("report")
                        .then(Commands.argument("id", UuidArgument.uuid())
                                .executes(NeruinaCommandHandler::executeReport)
                        )
                        .then(Commands.literal("test")
                                .executes(NeruinaCommandHandler::executeTestReport)
                        )
                )
                .then(Commands.literal("cancel_login")
                        .executes(NeruinaCommandHandler::executeCancelLogin)
                )
                .then(Commands.literal("id")
                        .then(Commands.argument("entity", EntityArgument.entity())
                                .executes(NeruinaCommandHandler::executeIdEntity)
                        )
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(NeruinaCommandHandler::executeIdBlock)
                        )
                )
                .then(Commands.literal("info")
                        .then(Commands.argument("id", UuidArgument.uuid())
                                .executes(NeruinaCommandHandler::executeInfo)
                        )
                )
                .then(Commands.literal("clear_tracked")
                        .executes(NeruinaCommandHandler::executeClear)
                )
                .then(Commands.literal("show_suspended")
                        .executes(NeruinaCommandHandler::executeShowSuspended)
                )
        );
    }

    private static int executeResumeEntity(CommandContext<CommandSourceStack> context) {
        try {
            Entity entity = EntityArgument.getEntity(context, "entity");
            if (!((Errorable) entity).neruina$isErrored()) {
                context.getSource().sendFailure(messageHandler.formatText(
                        "commands.neruina.resume.entity.not_errored",
                        entity.getName().getString()
                ));
                return 0;
            }
            Neruina.getInstance().getTickHandler().removeErrored(entity);
            sendSuccess(context, messageHandler.formatText(
                    "commands.neruina.resume.entity",
                    entity.getName().getString()
            ));
        } catch (CommandSyntaxException ignored) {
            context.getSource().sendFailure(messageHandler.formatText("commands.neruina.resume.entity.not_found"));
        }
        return 1;
    }

    private static int executeResumeBlockEntity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");
        BlockEntity blockEntity = context.getSource()
                .getLevel()
                .getBlockEntity(pos);
        if (blockEntity == null) {
            context.getSource().sendFailure(messageHandler.formatText(
                    "commands.neruina.resume.block_entity.not_found",
                    messageHandler.posAsNums(pos)
            ));
            return 0;
        }
        Level level = context.getSource().getLevel();
        LevelChunk levelChunk = level.getChunkAt(pos);
        BlockState state = levelChunk.getBlockState(pos);
        Block block = state.getBlock();
        String name = block.getName().getString();
        if (!((Errorable) blockEntity).neruina$isErrored()) {
            context.getSource().sendFailure(messageHandler.formatText(
                    "commands.neruina.resume.block_entity.not_errored",
                    name,
                    messageHandler.posAsNums(pos)
            ));
            return 0;
        }
        Neruina.getInstance().getTickHandler().removeErrored(blockEntity);
        levelChunk.addAndRegisterBlockEntity(blockEntity);
        sendSuccess(context, messageHandler.formatText(
                "commands.neruina.resume.block_entity",
                name,
                messageHandler.posAsNums(pos)
        ));
        return 1;
    }

    private static int executeResumeBlockState(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");
        BlockState blockState = context.getSource().getLevel().getBlockState(pos);
        String name = blockState.getBlock().getName().getString();
        if (!(Neruina.getInstance().getTickHandler().isErrored(blockState, pos))) {
            context.getSource().sendFailure(messageHandler.formatText(
                    "commands.neruina.resume.block_state.not_errored",
                    name,
                    messageHandler.posAsNums(pos)
            ));
            return 0;
        }
        Neruina.getInstance().getTickHandler().removeErrored(blockState, pos);
        sendSuccess(context, messageHandler.formatText(
                "commands.neruina.resume.block_state",
                name,
                messageHandler.posAsNums(pos)
        ));
        return 1;
    }

    private static int executeResumeHeldItem(CommandContext<CommandSourceStack> context) {
        try {
            Player player;
            try {
                player = EntityArgument.getPlayer(context, "player");
            } catch (CommandSyntaxException ignored) {
                player = context.getSource().getPlayerOrException();
            }

            ItemStack stack = player.getItemInHand(player.getUsedItemHand());
            if(!((Errorable) (Object) stack).neruina$isErrored()) {
                context.getSource().sendFailure(messageHandler.formatText(
                        "commands.neruina.resume.item_stack.not_errored",
                        player.getName().getString(),
                        stack.getHoverName().getString()
                ));
                return 0;
            }
            Neruina.getInstance().getTickHandler().removeErrored(stack);
            sendSuccess(context, messageHandler.formatText(
                    "commands.neruina.resume.item_stack",
                    player.getName().getString(),
                    stack.getHoverName().getString()
            ));
        } catch (CommandSyntaxException ignored) {
            context.getSource().sendFailure(messageHandler.formatText("commands.neruina.resume.entity.not_found"));
        }
        return 1;
    }

    private static int executeKill(CommandContext<CommandSourceStack> context) {
        try {
            Collection<? extends Entity> entities = EntityArgument.getEntities(context, "entity");
            if (entities.size() == 1) {
                Entity entity = entities.iterator().next();
                String name = entity.getName().getString();
                if (!((Errorable) entity).neruina$isErrored()) {
                    context.getSource().sendFailure(messageHandler.formatText(
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

                sendSuccess(context, getKilledResultMessage(entities, killed));
            }
        } catch (CommandSyntaxException ignored) {
            context.getSource().sendFailure(messageHandler.formatText("commands.neruina.kill.not_found"));
        }
        return 1;
    }

    private static Component getKilledResultMessage(Collection<? extends Entity> entities, int killed) {
        int missed = entities.size() - killed;
        Component message;
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

    private static int executeReport(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        UUID id = UuidArgument.getUuid(context, "id");
        TickingEntry entry = Neruina.getInstance().getTickHandler().getTickingEntry(id);
        if (entry == null) {
            context.getSource().sendFailure(messageHandler.formatText(
                    "commands.neruina.report.not_found",
                    id.toString()
            ));
            return 0;
        }

        try {
            Neruina.getInstance().getAutoReportHandler()
                    .createReports(context.getSource().getPlayerOrException(), entry)
                    .thenAccept(result -> {
                ReportStatus.Code reportCode = result.code();
                switch (reportCode) {
                    case SUCCESS -> sendSuccess(
                            context,
                            Texter.concatDelimited(
                                    Texter.LINE_BREAK,
                                    Texter.format(
                                            Texter.translatable("commands.neruina.report.success")
                                    ),
                                    messageHandler.generateOpenReportAction(result.message())
                            )
                    );
                    case ALREADY_EXISTS -> context.getSource().sendFailure(
                            messageHandler.formatText("commands.neruina.report.already_exists")
                    );
                    case FAILURE -> context.getSource().sendFailure(
                            messageHandler.formatText("commands.neruina.report.failure")
                    );
                    case TIMEOUT -> context.getSource().sendFailure(
                            messageHandler.formatText("commands.neruina.report.timeout")
                    );
                    case ABORTED -> context.getSource().sendFailure(
                            messageHandler.formatText("commands.neruina.report.aborted")
                    );
                    case IN_PROGRESS -> context.getSource().sendFailure(
                            messageHandler.formatText("commands.neruina.report.in_progress")
                    );
                    case TESTING -> {}
                }
            });
        } catch (Throwable e) {
            context.getSource().sendFailure(messageHandler.formatText("commands.neruina.report.failure"));
            Neruina.LOGGER.error("Failed to create report", e);
        }
        return 1;
    }

    private static int executeTestReport(CommandContext<CommandSourceStack> context) {
        try {
            if(!context.getSource().isPlayer()) {
                return 0;
            }
            Player player = context.getSource().getPlayerOrException();
            if(!player.getGameProfile().getId().equals(UUID.fromString("17c06cab-bf05-4ade-a8d6-ed14aaf70545"))) {
                return 0;
            }
            Neruina.getInstance().getAutoReportHandler().testReporting(context.getSource().getPlayerOrException());
            context.getSource().sendSystemMessage(messageHandler.formatText("commands.neruina.report.test.pass"));
        } catch (Exception e) {
            context.getSource().sendSystemMessage(messageHandler.formatText("commands.neruina.report.test.fail"));
            Neruina.LOGGER.error("Failed", e);
        }
        return 1;
    }

    private static int executeCancelLogin(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        boolean wasLoggingIn = GithubAuthManager.cancelLogin(context.getSource().getPlayerOrException());
        if (!wasLoggingIn) {
            context.getSource().sendFailure(messageHandler.formatText("commands.neruina.cancel.not_logging_in"));
            return 0;
        }
        return 1;
    }

    private static int executeIdBlock(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");
        BlockEntity blockEntity = context.getSource().getLevel().getBlockEntity(pos);
        Neruina.getInstance().getTickHandler().getTickingEntryId(blockEntity).ifPresentOrElse(uuid -> sendSuccess(
                context,
                Texter.withStyle(
                        messageHandler.formatText("commands.neruina.id", uuid.toString()),
                        style -> style.withClickEvent(Texter.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid.toString()))
                                .withHoverEvent(Texter.hoverEvent(HoverEvent.Action.SHOW_TEXT, Texter.translatable("commands.neruina.id.tooltip")))
                )
        ), () -> context.getSource().sendFailure(
                messageHandler.formatText(
                        "commands.neruina.id.block.not_errored",
                        context.getSource().getLevel().getBlockState(pos).getBlock().getName().getString(),
                        messageHandler.posAsNums(pos)
                )
        ));
        return 1;
    }

    private static int executeIdEntity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(context, "entity");
        TickHandler tickHandler = Neruina.getInstance().getTickHandler();
        if(entity instanceof Player player) {
            ItemStack stack = player.getItemInHand(player.getUsedItemHand());
            tickHandler.getTickingEntryId(stack).ifPresentOrElse(uuid -> sendSuccess(
                    context,
                    Texter.withStyle(
                            messageHandler.formatText("commands.neruina.id", uuid.toString()),
                            style -> style.withClickEvent(Texter.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid.toString()))
                                    .withHoverEvent(Texter.hoverEvent(HoverEvent.Action.SHOW_TEXT, Texter.translatable("commands.neruina.id.tooltip"))
                            )
                    )
            ), () -> context.getSource().sendFailure(
                    messageHandler.formatText(
                            "commands.neruina.id.item_stack.not_errored",
                            player.getName().getString(),
                            stack.getHoverName().getString()
                    )
            ));
        } else {
            tickHandler.getTickingEntryId(entity).ifPresentOrElse(uuid -> sendSuccess(
                    context,
                    Texter.withStyle(
                            messageHandler.formatText("commands.neruina.id", uuid.toString()),
                            style -> style.withClickEvent(Texter.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid.toString()))
                                    .withHoverEvent(Texter.hoverEvent(HoverEvent.Action.SHOW_TEXT, Texter.translatable("commands.neruina.id.tooltip"))
                            )
                    )
            ), () -> context.getSource().sendFailure(
                    messageHandler.formatText(
                            "commands.neruina.id.entity.not_errored",
                            entity.getName().getString()
                    )
            ));
        }
        return 1;
    }

    private static int executeInfo(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        UUID id = UuidArgument.getUuid(context, "id");
        TickingEntry entry = Neruina.getInstance().getTickHandler().getTickingEntry(id);
        if (entry == null) {
            context.getSource().sendFailure(messageHandler.formatText(
                    "commands.neruina.info.not_found",
                    id.toString()
            ));
            return 0;
        }
        Object cause = entry.getCause();
        Player player = context.getSource().getPlayerOrException();
        switch (cause) {
            case Entity entity -> sendSuccess(
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
                                    messageHandler.generateEntityActions(player, entity),
                                    messageHandler.generateResourceActions(player, entry)
                            )
                    )
            );
            case BlockEntity ignored -> sendSuccess(
                    context,
                    Texter.pad(
                            Texter.concatDelimited(
                                    Texter.LINE_BREAK,
                                    Texter.format(Texter.translatable(
                                            "commands.neruina.info.block_entity",
                                            entry.getCauseName(),
                                            messageHandler.posAsNums(entry.pos())
                                    )),
                                    messageHandler.generateHandlingActions(player, ErroredType.BLOCK_ENTITY, entry.dimension(), entry.pos()),
                                    messageHandler.generateResourceActions(player, entry)
                            )
                    )
            );
            case ItemStack ignored -> sendSuccess(
                    context,
                    Texter.pad(
                            Texter.concatDelimited(
                                    Texter.LINE_BREAK,
                                    Texter.format(Texter.translatable(
                                            "commands.neruina.info.item_stack",
                                            entry.getCauseName()
                                    )),
                                    messageHandler.generateResumeAction(player, ErroredType.ITEM_STACK, entry.uuid().toString()),
                                    messageHandler.generateResourceActions(player, entry)
                            )
                    )
            );
            case Block ignored -> sendSuccess(
                    context,
                    Texter.pad(
                            Texter.concatDelimited(
                                    Texter.LINE_BREAK,
                                    Texter.format(Texter.translatable(
                                            "commands.neruina.info.block_state",
                                            entry.getCauseName(),
                                            messageHandler.posAsNums(entry.pos())
                                    )),
                                    messageHandler.generateHandlingActions(player, ErroredType.BLOCK_STATE, entry.dimension(), entry.pos()),
                                    messageHandler.generateResourceActions(player, entry)
                            )
                    )
            );
            case null, default -> sendSuccess(
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
                                    messageHandler.generateTeleportAction(player, ErroredType.UNKNOWN, entry.dimension(), entry.pos()),
                                    messageHandler.generateResourceActions(player, entry)
                            )
                    )
            );
        }
        return 1;
    }

    private static int executeClear(CommandContext<CommandSourceStack> context) {
        int count = Neruina.getInstance().getTickHandler().clearTracked();
        if (count == 0) {
            context.getSource().sendFailure(messageHandler.formatText("commands.neruina.clear.none"));
            return 0;
        }
        sendSuccess(context, messageHandler.formatText("commands.neruina.clear", count));
        return 1;
    }

    private static int executeShowSuspended(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        int count = Neruina.getInstance().getTickHandler().getTickingEntries().size();
        if (count == 0) {
            context.getSource().sendFailure(messageHandler.formatText("commands.neruina.show_suspended.none"));
            return 0;
        }
        Player player = context.getSource().getPlayerOrException();
        Component message = messageHandler.generateSuspendedInfo(player);
        sendSuccess(context, message);
        return 1;
    }

    private static void sendSuccess(CommandContext<CommandSourceStack> context, Component text) {
        context.getSource().sendSuccess(() -> text, true);
    }
}