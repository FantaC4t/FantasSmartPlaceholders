package com.fantac4t.playerstatus.commands;

import com.fantac4t.playerstatus.config.PlayerDataConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

public final class SuffixCommand {

    private static final int MAX_LENGTH = 48;

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("suffix")
                .requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.literal("get")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                    String suf = PlayerDataConfig.getSuffix(target.getUUID());
                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("Suffix for " + target.getName().getString() + ": " + (suf.isEmpty() ? "<none>" : suf)),
                                            false);
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("clear")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                    PlayerDataConfig.clearSuffix(target.getUUID());
                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("Cleared suffix for " + target.getName().getString()),
                                            true);
                                    return 1;
                                })
                        )
                )
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("text", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                    String input = StringArgumentType.getString(ctx, "text").trim();
                                    final String value = input.length() > MAX_LENGTH ? input.substring(0, MAX_LENGTH) : input;
                                    PlayerDataConfig.setSuffix(target.getUUID(), value);
                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("Set suffix for " + target.getName().getString() + " to: " + value),
                                            true);
                                    return 1;
                                })
                        )
                )
        );
    }
}
