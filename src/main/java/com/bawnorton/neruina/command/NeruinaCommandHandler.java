package com.bawnorton.neruina.command;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.extend.Errorable;
import com.bawnorton.neruina.extend.ErrorableBlockState;
import com.bawnorton.neruina.version.VersionedText;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import java.util.Collection;

public class NeruinaCommandHandler {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("neruina")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("resume")
                                .then(CommandManager.literal("entity")
                                        .then(CommandManager.argument("entity", EntityArgumentType.entity())
                                                .executes(context -> {
                                                    try {
                                                        Entity entity = EntityArgumentType.getEntity(context, "entity");
                                                        if (!((Errorable) entity).neruina$isErrored()) {
                                                            context.getSource().sendError(
                                                                    getText(
                                                                            "commands.neruina.resume.entity.not_errored",
                                                                            "Target Enity [%s] not errored.",
                                                                            entity.getName().getString()
                                                                    )
                                                            );
                                                            return 0;
                                                        }
                                                        Neruina.TICK_HANDLER.removeErrored(entity);
                                                        context.getSource().sendFeedback(
                                                                /*? if >=1.20 */
                                                                () ->
                                                                getText(
                                                                        "commands.neruina.resume.entity",
                                                                        "Resumed ticking for Entity [%s] on request.",
                                                                        entity.getName().getString()
                                                                ),
                                                                true
                                                        );
                                                    } catch (CommandSyntaxException ignored) {
                                                    }
                                                    return 1;
                                                })
                                        )
                                )
                                .then(CommandManager.literal("block_entity")
                                        .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                                .executes(context -> {
                                                    BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
                                                    BlockEntity blockEntity = context.getSource()
                                                            .getWorld()
                                                            .getBlockEntity(pos);
                                                    if (blockEntity == null) {
                                                        context.getSource().sendError(
                                                                getText(
                                                                        "commands.neruina.resume.block_entity.not_found",
                                                                        "No Block Entity found at [%s].",
                                                                        formatPos(pos)
                                                                )
                                                        );
                                                        return 0;
                                                    }
                                                    World world = context.getSource().getWorld();
                                                    WorldChunk worldChunk = world.getWorldChunk(pos);
                                                    BlockState state = worldChunk.getBlockState(pos);
                                                    Block block = state.getBlock();
                                                    String name = block.getName().getString();
                                                    if (!((Errorable) blockEntity).neruina$isErrored()) {
                                                        context.getSource().sendError(
                                                                getText(
                                                                        "commands.neruina.resume.block_entity.not_errored",
                                                                        "Target Block Entity [%s] at [%s] is not errored.",
                                                                        name,
                                                                        formatPos(pos)
                                                                )
                                                        );
                                                        return 0;
                                                    }
                                                    Neruina.TICK_HANDLER.removeErrored(blockEntity);
                                                    worldChunk.addBlockEntity(blockEntity);
                                                    context.getSource().sendFeedback(
                                                            /*? if >=1.20 */
                                                            () ->
                                                            getText(
                                                                    "commands.neruina.resume.block_entity",
                                                                    "Resumed ticking for Block Entity [%s] at [%s] on request.",
                                                                    name,
                                                                    formatPos(pos)
                                                            ),
                                                            true
                                                    );
                                                    return 1;
                                                })
                                        )
                                )
                                .then(CommandManager.literal("block_state")
                                        .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                                .executes(context -> {
                                                    BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
                                                    BlockState blockState = context.getSource()
                                                            .getWorld()
                                                            .getBlockState(pos);
                                                    String name = blockState.getBlock().getName().getString();
                                                    if (!((ErrorableBlockState) blockState).neruina$isErrored(pos)) {
                                                        context.getSource().sendError(
                                                                getText(
                                                                        "commands.neruina.resume.block_state.not_errored",
                                                                        "Target Block [%s] at [%s] is not errored.",
                                                                        name,
                                                                        formatPos(pos)
                                                                )
                                                        );
                                                        return 0;
                                                    }
                                                    Neruina.TICK_HANDLER.removeErrored(blockState, pos);
                                                    context.getSource().sendFeedback(
                                                            /*? if >=1.20 */
                                                            () ->
                                                            getText(
                                                                    "commands.neruina.resume.block_state",
                                                                    "Resumed ticking for Block [%s] at [%s] on request.",
                                                                    name,
                                                                    formatPos(pos)
                                                            ),
                                                            true
                                                    );
                                                    return 1;
                                                })
                                        )
                                )
                        )
                        .then(CommandManager.literal("kill")
                                .then(CommandManager.argument("entity", EntityArgumentType.entities())
                                        .executes(context -> {
                                            try {
                                                Collection<? extends Entity> entities = EntityArgumentType.getEntities(context, "entity");
                                                if(entities.size() == 1) {
                                                    Entity entity = entities.iterator().next();
                                                    String name = entity.getName().getString();
                                                    if (!((Errorable) entity).neruina$isErrored()) {
                                                        context.getSource().sendError(
                                                                getText(
                                                                        "commands.neruina.kill.not_errored",
                                                                        "Target Entity [%s] is not errored, use /kill instead.",
                                                                        name
                                                                )
                                                        );
                                                        return 0;
                                                    }
                                                    Neruina.TICK_HANDLER.killEntity(entity, VersionedText.translatableWithFallback(
                                                            "commands.neruina.kill",
                                                            "Killed Entity [%s] on request.",
                                                            name
                                                    ));
                                                } else {
                                                    int killed = 0;
                                                    for(Entity entity : entities) {
                                                        if (!((Errorable) entity).neruina$isErrored()) continue;

                                                        Neruina.TICK_HANDLER.killEntity(entity, null);
                                                        killed++;
                                                    }
                                                    int missed = entities.size() - killed;
                                                    // grammar moment
                                                    Text message;
                                                    if(killed == 1 && missed == 1) {
                                                        message = getText(
                                                                "commands.neruina.kill.multiple.singular_singular",
                                                                "Killed 1 errored entity on request. 1 was not errored."
                                                        );
                                                    } else if (killed == 1) {
                                                        message = getText(
                                                                "commands.neruina.kill.multiple.singular_plural",
                                                                "Killed 1 errored entity on request. %s were not errored.",
                                                                missed
                                                        );
                                                    } else if (missed == 1) {
                                                        message = getText(
                                                                "commands.neruina.kill.multiple.plural_singular",
                                                                "Killed %s errored entities on request. 1 was not errored.",
                                                                killed
                                                        );
                                                    } else {
                                                        message = getText(
                                                                "commands.neruina.kill.multiple",
                                                                "Killed %s errored entities on request. %s were not errored.",
                                                                killed,
                                                                missed
                                                        );
                                                    }
                                                    Neruina.TICK_HANDLER.broadcastToPlayers(context.getSource().getServer(), message);
                                                }
                                            } catch (CommandSyntaxException ignored) {
                                            }
                                            return 1;
                                        })
                                )
                        )
        );
    }

    private static String formatPos(BlockPos pos) {
        return "x=%s y=%s z=%s".formatted(pos.getX(), pos.getY(), pos.getZ());
    }

    private static Text getText(String key, String fallback, Object... args) {
        return VersionedText.format(VersionedText.translatableWithFallback(key, fallback, args));
    }
}
