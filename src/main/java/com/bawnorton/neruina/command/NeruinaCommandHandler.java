package com.bawnorton.neruina.command;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.extend.Errorable;
import com.bawnorton.neruina.version.VersionedText;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class NeruinaCommandHandler {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("neruina")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("resume")
                                .then(CommandManager.argument("entity", EntityArgumentType.entity())
                                        .executes(context -> {
                                            try {
                                                Entity entity = EntityArgumentType.getEntity(context, "entity");
                                                if (!((Errorable) entity).neruina$isErrored()) {
                                                    context.getSource().sendError(
                                                            getText(
                                                                    "commands.neruina.resume.not_errored",
                                                                    "Target Entity [%s] is not errored.",
                                                                    entity.getName().getString()
                                                            )
                                                    );
                                                    return 0;
                                                }
                                                Neruina.TICK_HANDLER.removeErrored(entity);
                                                context.getSource().sendFeedback(
                                                        /*? if >=1.20 */
                                                        /*() ->*/
                                                        getText(
                                                                "commands.neruina.resume",
                                                                "Resumed Entity [%s] on request.",
                                                                entity.getName().getString()
                                                        ),
                                                        true
                                                );
                                            } catch (CommandSyntaxException ignored) {}
                                            return 1;
                                        })
                                )
                        )
                        .then(CommandManager.literal("kill")
                                .then(CommandManager.argument("entity", EntityArgumentType.entity())
                                        .executes(context -> {
                                            try {
                                                Entity entity = EntityArgumentType.getEntity(context, "entity");
                                                if (!((Errorable) entity).neruina$isErrored()) {
                                                    context.getSource().sendError(
                                                            getText(
                                                                    "commands.neruina.kill.not_errored",
                                                                    "Target Entity [%s] is not errored, use /kill instead.",
                                                                    entity.getName().getString()
                                                            )
                                                    );
                                                    return 0;

                                                }
                                                Neruina.TICK_HANDLER.killEntity(entity, VersionedText.translatableWithFallback(
                                                        "commands.neruina.kill",
                                                        "Killed Entity [%s] on request.",
                                                        entity.getName().getString()
                                                ));
                                            } catch (CommandSyntaxException ignored) {}
                                            return 1;
                                        })
                                )
                        )
        );
    }

    private static Text getText(String key, String fallback, Object... args) {
        return VersionedText.format(VersionedText.translatableWithFallback(key, fallback, args));
    }
}
