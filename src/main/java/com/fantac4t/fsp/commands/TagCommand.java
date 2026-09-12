package com.fantac4t.fsp.commands;

import com.fantac4t.fsp.config.PlayerDataConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class TagCommand {

    private static final int MAX_LENGTH = 48;

    /** A player this command is acting on, which may or may not currently be online. */
    private record Target(UUID uuid, String name) {}

    /** Online players first, then everyone we've stored data for, so offline players are completable too. */
    private static final SuggestionProvider<CommandSourceStack> KNOWN_PLAYERS = (ctx, builder) -> {
        Set<String> names = new LinkedHashSet<>();
        for (ServerPlayer p : ctx.getSource().getServer().getPlayerList().getPlayers()) {
            names.add(p.getName().getString());
        }
        names.addAll(PlayerDataConfig.knownNames());
        return SharedSuggestionProvider.suggest(names, builder);
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tag")
                .requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.literal("set")
                        .then(Commands.argument("player", StringArgumentType.word()).suggests(KNOWN_PLAYERS)
                                .then(Commands.argument("text", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            Target target = resolve(ctx.getSource(), StringArgumentType.getString(ctx, "player"));
                                            if (target == null) return 0;

                                            String input = StringArgumentType.getString(ctx, "text").trim();
                                            boolean truncated = input.length() > MAX_LENGTH;
                                            String value = truncated ? input.substring(0, MAX_LENGTH) : input;
                                            PlayerDataConfig.setNametag(target.uuid(), value);

                                            MutableComponent msg = Component.literal("Set nametag for ")
                                                    .withStyle(ChatFormatting.GREEN)
                                                    .append(Component.literal(target.name()).withStyle(ChatFormatting.YELLOW))
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
                        .then(Commands.argument("player", StringArgumentType.word()).suggests(KNOWN_PLAYERS)
                                .executes(ctx -> {
                                    Target target = resolve(ctx.getSource(), StringArgumentType.getString(ctx, "player"));
                                    if (target == null) return 0;

                                    String raw = PlayerDataConfig.getNametag(target.uuid());
                                    String suf = (raw != null) ? raw : "";
                                    boolean hasTag = !suf.isEmpty();

                                    MutableComponent msg = Component.literal("Nametag for ")
                                            .withStyle(ChatFormatting.GRAY)
                                            .append(Component.literal(target.name()).withStyle(ChatFormatting.YELLOW))
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
                        .then(Commands.argument("player", StringArgumentType.word()).suggests(KNOWN_PLAYERS)
                                .executes(ctx -> {
                                    Target target = resolve(ctx.getSource(), StringArgumentType.getString(ctx, "player"));
                                    if (target == null) return 0;

                                    PlayerDataConfig.clearNametag(target.uuid());

                                    MutableComponent msg = Component.literal("Removed nametag for ")
                                            .withStyle(ChatFormatting.GREEN)
                                            .append(Component.literal(target.name()).withStyle(ChatFormatting.YELLOW));

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
                                // Fall back to the stored name so offline entries stay actionable —
                                // a bare UUID can't be passed back to /tag remove.
                                String name = online != null ? online.getName().getString()
                                        : displayNameFor(uuid);
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

    /** Resolves a name to a target, preferring the online player. Reports the failure itself and returns null if unknown. */
    private static Target resolve(CommandSourceStack src, String name) {
        ServerPlayer online = src.getServer().getPlayerList().getPlayerByName(name);
        if (online != null) return new Target(online.getUUID(), online.getName().getString());

        Optional<UUID> offline = PlayerDataConfig.findUuidByName(name);
        if (offline.isPresent()) return new Target(offline.get(), name);

        src.sendFailure(Component.literal(
            "Player '" + name + "' not found — they must have joined the server at least once."));
        return null;
    }

    private static String displayNameFor(UUID uuid) {
        String stored = PlayerDataConfig.getStoredName(uuid);
        return (stored != null && !stored.isBlank()) ? stored : uuid.toString();
    }
}
