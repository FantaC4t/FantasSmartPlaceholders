package com.fantac4t.playerstatus.commands;

import com.fantac4t.playerstatus.player.NoSleepManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class NoSleepCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nosleep")
            .executes(ctx -> {
                boolean enabled = NoSleepManager.toggle(ctx.getSource().getPlayerOrException());
                return enabled ? 1 : 0;
            })
        );
    }
}
