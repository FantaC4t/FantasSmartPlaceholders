package com.fantac4t.fsp.commands;

import com.fantac4t.fsp.config.PlayerDataConfig;
import com.fantac4t.fsp.util.RGBColorProcessor;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class ColorCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("color")
            .executes(ctx -> {
                var player = ctx.getSource().getPlayerOrException();
                String current = PlayerDataConfig.getColor(player.getUUID());
                if (current == null || current.isBlank()) {
                    ctx.getSource().sendSuccess(() -> Component.literal("You have no color set. Use /color <hex> to set one."), false);
                } else {
                    Component coloredName = RGBColorProcessor.getColoredPlayerName(player.getName().getString(), current);
                    MutableComponent msg = Component.literal("Your current color is " + current + ". Your name looks like: ")
                        .append(coloredName);
                    ctx.getSource().sendSuccess(() -> msg, false);
                }
                return 1;
            })
            .then(Commands.argument("value", StringArgumentType.greedyString())
                .executes(ctx -> {
                    var player = ctx.getSource().getPlayerOrException();
                    String value = StringArgumentType.getString(ctx, "value");

                    if (!RGBColorProcessor.isValid(value)) {
                        ctx.getSource().sendFailure(Component.literal("Invalid color value."));
                        return 0;
                    }

                    // Normalize the input by adding # if needed
                    value = RGBColorProcessor.normalizeColorInput(value);

                    PlayerDataConfig.setColor(player.getUUID(), value);

                    // Create a preview message with colored name
                    String playerName = player.getName().getString();
                    Component coloredName = RGBColorProcessor.getColoredPlayerName(playerName, value);

                    MutableComponent message = Component.literal("Your color has been set to " + value + ". Your name now looks like this: ")
                        .append(coloredName);
                    ctx.getSource().sendSuccess(() -> message, false);
                    return 1;
                }))
            .then(Commands.literal("clear")
                .executes(ctx -> {
                    var player = ctx.getSource().getPlayerOrException();
                    PlayerDataConfig.clearColor(player.getUUID());
                    ctx.getSource().sendSuccess(() -> Component.literal("Color cleared. Your name is now displayed without color."), false);
                    return 1;
                }))
            .then(Commands.literal("gradient")
                .then(Commands.argument("colors", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        var player = ctx.getSource().getPlayerOrException();
                        // greedyString so "#" is accepted, same as the singular /color <hex> path.
                        String[] tokens = StringArgumentType.getString(ctx, "colors").trim().split("\\s+");
                        if (tokens.length != 2) {
                            ctx.getSource().sendFailure(Component.literal("Usage: /color gradient <hex1> <hex2> — e.g. /color gradient #FF0000 #0000FF"));
                            return 0;
                        }

                        String hex1 = RGBColorProcessor.normalizeColorInput(tokens[0]);
                        String hex2 = RGBColorProcessor.normalizeColorInput(tokens[1]);

                        if (!RGBColorProcessor.isValid(hex1) || !hex1.startsWith("#")) {
                            ctx.getSource().sendFailure(Component.literal("Invalid first color — use a hex code like FF0000 or #FF0000."));
                            return 0;
                        }
                        if (!RGBColorProcessor.isValid(hex2) || !hex2.startsWith("#")) {
                            ctx.getSource().sendFailure(Component.literal("Invalid second color — use a hex code like 0000FF or #0000FF."));
                            return 0;
                        }

                        String stored = "gradient:" + hex1 + ":" + hex2;
                        PlayerDataConfig.setColor(player.getUUID(), stored);

                        Component preview = RGBColorProcessor.getColoredPlayerName(player.getName().getString(), stored);
                        MutableComponent msg = Component.literal("Gradient set! Your name now looks like: ").append(preview);
                        ctx.getSource().sendSuccess(() -> msg, false);
                        return 1;
                    })))
        );
    }
}