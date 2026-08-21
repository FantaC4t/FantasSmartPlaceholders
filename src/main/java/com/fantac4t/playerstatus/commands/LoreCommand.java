package com.fantac4t.playerstatus.commands;

import com.fantac4t.playerstatus.config.PlayerDataConfig;
import com.fantac4t.playerstatus.util.TextUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class LoreCommand {

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
                                       .append(TextUtil.parseMini(lore)),
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
                    String text = StringArgumentType.getString(ctx, "text");
                    PlayerDataConfig.setLore(player.getUUID(), text);
                    ctx.getSource().sendSuccess(
                        () -> Component.literal("Lore set: ").withStyle(ChatFormatting.GRAY)
                                       .append(TextUtil.parseMini(text)),
                        false);
                    return 1;
                }))
        );
    }
}
