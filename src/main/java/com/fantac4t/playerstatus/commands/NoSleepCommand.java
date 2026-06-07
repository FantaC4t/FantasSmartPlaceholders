package com.fantac4t.playerstatus.commands;

import com.fantac4t.playerstatus.player.NoSleepManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class NoSleepCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nosleep")
            .executes(ctx -> {
                NoSleepManager.toggle(ctx.getSource().getPlayerOrException());
                return 1;
            })
        );
    }
}
