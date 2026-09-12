package com.fantac4t.fsp.commands;

import com.fantac4t.fsp.FSP;
import com.fantac4t.fsp.config.PlayerDataConfig;
import com.fantac4t.fsp.util.RGBColorProcessor;
import com.fantac4t.fsp.util.TextUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
//? if mc26 {
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.ServerPlaceholderContext;
//?} else {
/*import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
*///?}
import net.fabricmc.loader.api.FabricLoader;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.UUID;

public final class ProfileCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("profile")
            .executes(ctx -> {
                ServerPlayer self = ctx.getSource().getPlayerOrException();
                sendProfile(ctx.getSource(), self.getUUID(), self.getName().getString(), self);
                return 1;
            })
            .then(Commands.argument("player", StringArgumentType.word())
                .executes(ctx -> {
                    String name = StringArgumentType.getString(ctx, "player");
                    CommandSourceStack src = ctx.getSource();

                    ServerPlayer online = src.getServer().getPlayerList().getPlayerByName(name);
                    if (online != null) {
                        sendProfile(src, online.getUUID(), online.getName().getString(), online);
                        return 1;
                    }

                    Optional<UUID> offlineUuid = PlayerDataConfig.findUuidByName(name);
                    if (offlineUuid.isEmpty()) {
                        src.sendFailure(Component.literal("Player '" + name + "' not found."));
                        return 0;
                    }
                    sendProfile(src, offlineUuid.get(), name, null);
                    return 1;
                }))
        );
    }

    private static void sendProfile(CommandSourceStack src, UUID uuid, String name, ServerPlayer onlinePlayer) {
        send(src, parse("<gray>━━ </gray><gold><bold>Profile: " + name + "</bold></gold><gray> ━━</gray>"));

        // Name (colored)
        String color = PlayerDataConfig.getColor(uuid);
        Component coloredName = RGBColorProcessor.getColoredPlayerName(name, color != null ? color : "");
        send(src, row("Name", coloredName));

        // Pronouns — only resolvable when online
        if (onlinePlayer != null) {
            String pronouns = resolvePronouns(onlinePlayer);
            if (!pronouns.isBlank()) {
                send(src, row("Pronouns", plain(pronouns)));
            }
        }

        // Role (LuckPerms — works offline if LP has cached the user)
        String role = resolveRole(uuid);
        String roleSymbol = FSP.CONFIG.roles.groups.getOrDefault(role, "");
        if (!roleSymbol.isBlank()) {
            send(src, row("Role", parse(roleSymbol)));
        } else if (!role.isBlank()) {
            send(src, row("Role", plain(role)));
        }

        // Live
        boolean live = PlayerDataConfig.isLive(uuid);
        String link = PlayerDataConfig.getLink(uuid);
        String normLink = TextUtil.normalizeUrl(link);

        StringBuilder liveText = new StringBuilder(live ? "<red>LIVE</red>" : "<gray>offline</gray>");
        if (normLink != null) {
            String u = esc(normLink);
            liveText.append("<gray> · </gray>")
                .append("<hover:show_text:'<gray>Click to open: ").append(u).append("</gray>'>")
                .append("<click:open_url:'").append(u).append("'><aqua><underlined>").append(esc(link)).append("</underlined></aqua></click>")
                .append("</hover>");
        }
        send(src, row("Live", parse(liveText.toString())));

        // Suffix / Nametag — admin-set, so full MiniMessage is allowed
        String nametag = PlayerDataConfig.getNametag(uuid);
        send(src, row("Suffix", nametag != null && !nametag.isBlank() ? parse(nametag) : dim("—")));

        // Lore — player-set, so parse with the safe tag set (no click/hover/etc.)
        String lore = PlayerDataConfig.getLore(uuid);
        send(src, row("Lore", lore != null && !lore.isBlank()
            ? Component.empty().withStyle(ChatFormatting.ITALIC).append(TextUtil.parseUserMini(lore))
            : dim("—")));

        // No-Sleep
        send(src, row("No-Sleep", PlayerDataConfig.isNoSleep(uuid)
            ? parse("<yellow>active</yellow>")
            : dim("inactive")));
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private static MutableComponent row(String label, Component value) {
        return Component.literal("  ")
            .append(Component.literal(label).withStyle(ChatFormatting.GRAY))
            .append(Component.literal("  "))
            .append(value);
    }

    private static String resolvePronouns(ServerPlayer player) {
        try {
            //? if mc26 {
        Component resolved = Placeholders.SERVER_PLACEHOLDER_PARSER.parseComponent(
                "%playerpronouns:pronouns%",
                ParserContext.of(ServerPlaceholderContext.SERVER_KEY, ServerPlaceholderContext.of(player)));
        //?} else {
        /*Component resolved = Placeholders.parseText(
                Component.literal("%playerpronouns:pronouns%"),
                PlaceholderContext.of(player));
        *///?}
            String text = resolved.getString();
            return text != null ? text.trim() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private static String resolveRole(UUID uuid) {
        if (!FabricLoader.getInstance().isModLoaded("luckperms")) return "";
        try {
            User user = LuckPermsProvider.get().getUserManager().getUser(uuid);
            return user != null ? user.getPrimaryGroup() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private static Component parse(String mini)  { return TextUtil.parseMini(mini); }
    private static Component plain(String text)  { return Component.literal(text).withStyle(ChatFormatting.WHITE); }
    private static Component dim(String text)    { return Component.literal(text).withStyle(ChatFormatting.GRAY); }
    private static String esc(String s)          { return TextUtil.escapeMini(s); }

    private static void send(CommandSourceStack src, Component c) {
        src.sendSuccess(() -> c, false);
    }
}
