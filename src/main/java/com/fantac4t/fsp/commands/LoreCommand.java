package com.fantac4t.fsp.commands;

import com.fantac4t.fsp.config.PlayerDataConfig;
import com.fantac4t.fsp.util.TextUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class LoreCommand {

    private static final int MAX_LENGTH = 256;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("lore")
            .executes(ctx -> {
                ServerPlayer player = ctx.getSource().getPlayerOrException();
                String lore = PlayerDataConfig.getLore(player.getUUID());
                if (lore == null || lore.isBlank()) {
                    ctx.getSource().sendSuccess(
                        () -> Component.literal("You have no lore set. Use /lore <text> to set one.")
                                       .withStyle(ChatFormatting.GRAY),
                        false);
                } else {
                    ctx.getSource().sendSuccess(
                        () -> Component.literal("Your lore: ").withStyle(ChatFormatting.GRAY)
                                       .append(TextUtil.parseUserMini(lore)),
                        false);
                }
                return 1;
            })
            .then(Commands.literal("clear")
                .executes(ctx -> {
                    PlayerDataConfig.clearLore(ctx.getSource().getPlayerOrException().getUUID());
                    ctx.getSource().sendSuccess(
                        () -> Component.literal("Lore cleared.").withStyle(ChatFormatting.YELLOW),
                        false);
                    return 1;
                }))
            .then(Commands.argument("text", StringArgumentType.greedyString())
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    String input = StringArgumentType.getString(ctx, "text").trim();
                    boolean truncated = input.length() > MAX_LENGTH;
                    String text = truncated ? input.substring(0, MAX_LENGTH) : input;
                    PlayerDataConfig.setLore(player.getUUID(), text);
                    ctx.getSource().sendSuccess(
                        () -> {
                            var msg = Component.literal("Lore set: ").withStyle(ChatFormatting.GRAY)
                                           .append(TextUtil.parseUserMini(text));
                            if (truncated) {
                                msg.append(Component.literal(" (truncated to " + MAX_LENGTH + " characters)")
                                    .withStyle(ChatFormatting.RED));
                            }
                            return msg;
                        },
                        false);
                    return 1;
                }))
        );
    }
}
