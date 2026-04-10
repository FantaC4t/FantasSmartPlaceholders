package com.fantac4t.playerstatus.placeholders;

import com.fantac4t.playerstatus.PlayerStatus;
import com.fantac4t.playerstatus.config.PlayerDataConfig;
import com.fantac4t.playerstatus.player.LiveManager;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public final class LivePlaceholder {

    private static final Identifier LIVE_ID = Identifier.fromNamespaceAndPath(PlayerStatus.MOD_ID, "live");
    private static final Identifier STREAM_ID = Identifier.fromNamespaceAndPath(PlayerStatus.MOD_ID, "stream");
    private static final Identifier LIVE_STREAM_ID = Identifier.fromNamespaceAndPath(PlayerStatus.MOD_ID, "live_stream");
    private static final Identifier CLICKABLE_STREAM_ID = Identifier.fromNamespaceAndPath(PlayerStatus.MOD_ID, "clickable_stream");

    private LivePlaceholder() {}

    public static void register() {
        // Returns LIVE / not live as a pre-parsed Component
        Placeholders.register(LIVE_ID, (ctx, arg) -> {
            UUID target = resolvePlayerUuid(ctx, arg).orElse(null);
            String raw = LiveManager.placeholder(target);
            return PlaceholderResult.value(parseMini(raw));
        });

        // Returns the raw stream link as literal
        Placeholders.register(STREAM_ID, (ctx, arg) -> {
            UUID target = resolvePlayerUuid(ctx, arg).orElse(null);
            if (target == null || !PlayerDataConfig.isLive(target)) return PlaceholderResult.value(Component.empty());
            String link = PlayerDataConfig.getLink(target);
            return PlaceholderResult.value(link == null ? Component.empty() : Component.literal(link));
        });

        // Emits "LIVE <link>" when live
        Placeholders.register(LIVE_STREAM_ID, (ctx, arg) -> {
            UUID target = resolvePlayerUuid(ctx, arg).orElse(null);
            if (target == null || !PlayerDataConfig.isLive(target)) return PlaceholderResult.value(Component.empty());
            Component live = parseMini(LiveManager.placeholder(target));
            String link = PlayerDataConfig.getLink(target);
            if (link == null || link.isBlank()) return PlaceholderResult.value(live);
            return PlaceholderResult.value(live.copy().append(Component.literal(" " + link)));
        });

        // Clickable+hoverable link using MiniMessage formatting
        Placeholders.register(CLICKABLE_STREAM_ID, (ctx, arg) -> {
            UUID target = resolvePlayerUuid(ctx, arg).orElse(null);
            if (target == null || !PlayerDataConfig.isLive(target)) return PlaceholderResult.value(Component.empty());

            String url = normalizeUrl(PlayerDataConfig.getLink(target));
            if (url == null) return PlaceholderResult.value(Component.empty());

            String label = (arg != null && !arg.isBlank()) ? arg.trim() : "Watch Stream";
            String hover = "Click to open: " + url;
            
            // Use MiniMessage-style formatting that TextParserUtils can parse
            String formatted = "<hover:show_text:'" + esc(hover) + "'><click:open_url:'" + esc(url) + "'><aqua><underlined>" + esc(label) + "</underlined></aqua></click></hover>";
            Component parsed = TextParserUtils.formatText(formatted);
            
            return PlaceholderResult.value(parsed);
        });
    }

    private static Optional<UUID> resolvePlayerUuid(PlaceholderContext ctx, String rawArg) {
        if (rawArg != null && !rawArg.isBlank()) {
            UUID u = tryParseUuid(rawArg.trim());
            if (u != null) return Optional.of(u);
            UUID byName = lookupOnlinePlayerUuid(ctx, rawArg.trim());
            if (byName != null) return Optional.of(byName);
        }
        if (ctx != null && ctx.player() != null) {
            return Optional.of(ctx.player().getUUID());
        }
        return Optional.empty();
    }

    private static UUID tryParseUuid(String s) {
        try {
            return UUID.fromString(s);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static UUID lookupOnlinePlayerUuid(PlaceholderContext ctx, String name) {
        if (ctx == null || ctx.player() == null) return null;
        ServerPlayer any = ctx.player();
        MinecraftServer server = ((ServerLevel) any.level()).getServer();
        if (server == null) return null;
        ServerPlayer target = server.getPlayerList().getPlayerByName(name);
        return target != null ? target.getUUID() : null;
    }

    private static Component parseMini(String text) {
        if (text == null || text.isEmpty()) return Component.empty();
        return TextParserUtils.formatText(text);
    }

    private static final Pattern HTTP = Pattern.compile("(?i)^https?://.+");

    private static String normalizeUrl(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        if (!HTTP.matcher(t).matches()) t = "https://" + t;
        try {
            URI.create(t);
            return t;
        } catch (Exception e) {
            return null;
        }
    }
    
    // Escape single quotes for MiniMessage
    private static String esc(String s) {
        return s.replace("'", "\\'");
    }
}