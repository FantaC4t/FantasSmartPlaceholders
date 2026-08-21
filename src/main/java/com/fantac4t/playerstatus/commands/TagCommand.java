package com.fantac4t.playerstatus.commands;

import com.fantac4t.playerstatus.config.PlayerDataConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import java.util.Map;
import java.util.UUID;

public final class TagCommand {

    private static final int MAX_LENGTH = 48;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tag")
                .requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.literal("set")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("text", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                            String input = StringArgumentType.getString(ctx, "text").trim();
                                            boolean truncated = input.length() > MAX_LENGTH;
                                            String value = truncated ? input.substring(0, MAX_LENGTH) : input;
                                            PlayerDataConfig.setNametag(target.getUUID(), value);

                                            MutableComponent msg = Component.literal("Set nametag for ")
                                                    .withStyle(ChatFormatting.GREEN)
                                                    .append(Component.literal(target.getName().getString()).withStyle(ChatFormatting.YELLOW))
                                                    .append(Component.literal(" to: ").withStyle(ChatFormatting.GREEN))
                                                    .append(Component.literal(value).withStyle(ChatFormatting.WHITE));
                                            if (truncated) {
                                                msg.append(Component.literal(" (truncated to " + MAX_LENGTH + " characters)").withStyle(ChatFormatting.RED));
                                            }
                                            ctx.getSource().sendSuccess(() -> msg, true);
                                            return 1;
                                        })
                                )
                        )
                )
                .then(Commands.literal("get")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                    String raw = PlayerDataConfig.getNametag(target.getUUID());
                                    String suf = (raw != null) ? raw : "";
                                    boolean hasTag = !suf.isEmpty();

                                    MutableComponent msg = Component.literal("Nametag for ")
                                            .withStyle(ChatFormatting.GRAY)
                                            .append(Component.literal(target.getName().getString()).withStyle(ChatFormatting.YELLOW))
                                            .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                                            .append(hasTag
                                                    ? Component.literal(suf).withStyle(ChatFormatting.WHITE)
                                                    : Component.literal("<none>").withStyle(ChatFormatting.GRAY));

                                    ctx.getSource().sendSuccess(() -> msg, false);
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("remove")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                    PlayerDataConfig.clearNametag(target.getUUID());

                                    MutableComponent msg = Component.literal("Removed nametag for ")
                                            .withStyle(ChatFormatting.GREEN)
                                            .append(Component.literal(target.getName().getString()).withStyle(ChatFormatting.YELLOW));

                                    ctx.getSource().sendSuccess(() -> msg, true);
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            Map<UUID, String> tagged = PlayerDataConfig.getNametaggedPlayers();
                            if (tagged.isEmpty()) {
                                ctx.getSource().sendSuccess(
                                    () -> Component.literal("No players have a custom nametag set.").withStyle(ChatFormatting.GRAY),
                                    false);
                                return 1;
                            }

                            MinecraftServer server = ctx.getSource().getServer();
                            ctx.getSource().sendSuccess(
                                () -> Component.literal("── Custom Nametags (" + tagged.size() + ") ──").withStyle(ChatFormatting.GRAY),
                                false);

                            for (Map.Entry<UUID, String> entry : tagged.entrySet()) {
                                UUID uuid = entry.getKey();
                                String tag = entry.getValue();
                                ServerPlayer online = server.getPlayerList().getPlayer(uuid);
                                String name = online != null ? online.getName().getString() : uuid.toString();
                                ctx.getSource().sendSuccess(
                                    () -> Component.literal(" ")
                                            .append(Component.literal(name).withStyle(ChatFormatting.YELLOW))
                                            .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                                            .append(Component.literal(tag).withStyle(ChatFormatting.WHITE)),
                                    false);
                            }
                            return 1;
                        })
                )
        );
    }
}
