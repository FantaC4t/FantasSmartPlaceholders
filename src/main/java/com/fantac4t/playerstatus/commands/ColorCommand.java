package com.fantac4t.playerstatus.commands;

import com.fantac4t.playerstatus.config.PlayerDataConfig;
import com.fantac4t.playerstatus.util.RGBColorProcessor;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class ColorCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("color")
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
                    
                    MutableComponent message = Component.literal("Your color has been set to " + value + ".\nYour name now looks like this: ")
                        .append(coloredName);
                    
                    ctx.getSource().sendSuccess(() -> message, false);
                    player.sendSystemMessage(Component.literal("Color set to " + value));
                    return 1;
                }))
            .then(Commands.literal("clear")
                .executes(ctx -> {
                    var player = ctx.getSource().getPlayerOrException();
                    PlayerDataConfig.clearColor(player.getUUID());
                    ctx.getSource().sendSuccess(() -> Component.literal("Color cleared. Your name is now displayed without color."), false);
                    player.sendSystemMessage(Component.literal("Color cleared"));
                    return 1;
                }))
        );
    }
}