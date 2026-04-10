package com.fantac4t.playerstatus.commands;

import com.fantac4t.playerstatus.player.LiveManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class LiveCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("live")
            .executes(ctx -> {
                boolean live = LiveManager.toggleLive(ctx.getSource().getPlayerOrException());
                return live ? 1 : 0;
            })
            .then(Commands.literal("autoLiveOnDisconnect")
                .executes(ctx -> {
                    boolean enabled = LiveManager.togglePersist(ctx.getSource().getPlayerOrException());
                    return enabled ? 1 : 0;
                }))
            .then(Commands.literal("link")
                .then(Commands.argument("url", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        String link = StringArgumentType.getString(ctx, "url");
                        LiveManager.setLink(ctx.getSource().getPlayerOrException(), link);
                        return 1;
                    })))
        );
    }
}