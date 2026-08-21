package com.fantac4t.playerstatus.commands;

import com.fantac4t.playerstatus.PlayerStatus;
import com.fantac4t.playerstatus.config.ModConfig;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;

public final class FspCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("fsp")
            .requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
            .then(Commands.literal("reload")
                .executes(ctx -> {
                    PlayerStatus.CONFIG = ModConfig.load();
                    ctx.getSource().sendSuccess(
                        () -> Component.literal("Config reloaded.").withStyle(ChatFormatting.GREEN),
                        true);
                    return 1;
                }))
        );
    }
}
