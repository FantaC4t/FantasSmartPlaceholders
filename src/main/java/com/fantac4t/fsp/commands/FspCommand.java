package com.fantac4t.fsp.commands;

import com.fantac4t.fsp.FSP;
import com.fantac4t.fsp.config.ModConfig;
import com.fantac4t.fsp.config.PlayerDataConfig;
import com.fantac4t.fsp.player.TwitchManager;
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
                    FSP.CONFIG = ModConfig.load();
                    // Both re-read the fresh config and restart or stop themselves as needed.
                    TwitchManager.start(ctx.getSource().getServer());
                    PlayerDataConfig.startAutosave();
                    ctx.getSource().sendSuccess(
                        () -> Component.literal("Config reloaded.").withStyle(ChatFormatting.GREEN),
                        true);
                    return 1;
                }))
        );
    }
}
