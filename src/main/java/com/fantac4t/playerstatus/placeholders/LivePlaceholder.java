package com.fantac4t.playerstatus.placeholders;

import com.fantac4t.playerstatus.PlayerStatus;
import com.fantac4t.playerstatus.config.PlayerDataConfig;
import com.fantac4t.playerstatus.player.LiveManager;
import com.fantac4t.playerstatus.util.TextUtil;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.UUID;

public final class LivePlaceholder {

    private static final Identifier LIVE_ID             = Identifier.fromNamespaceAndPath(PlayerStatus.MOD_ID, "live");
    private static final Identifier STREAM_ID           = Identifier.fromNamespaceAndPath(PlayerStatus.MOD_ID, "stream");
    private static final Identifier LIVE_STREAM_ID      = Identifier.fromNamespaceAndPath(PlayerStatus.MOD_ID, "live_stream");
    private static final Identifier CLICKABLE_STREAM_ID = Identifier.fromNamespaceAndPath(PlayerStatus.MOD_ID, "clickable_stream");
    private static final Identifier LIVE_COUNT_ID       = Identifier.fromNamespaceAndPath(PlayerStatus.MOD_ID, "live_count");

    private LivePlaceholder() {}

    public static void register() {
        //? if mc26 {
        Placeholders.registerServer(LIVE_ID, (ctx, arg) -> {
        //?} else {
        /*Placeholders.register(LIVE_ID, (ctx, arg) -> {
        *///?}
            UUID target = resolvePlayerUuid(ctx, arg).orElse(null);
            if (target == null) return PlaceholderResult.value(Component.empty());
            String badge = LiveManager.placeholder(target);
            if (badge.isBlank()) return PlaceholderResult.value(Component.empty());
            String url = TextUtil.normalizeUrl(PlayerDataConfig.getLink(target));
            if (url != null) {
                String mini = "<hover:show_text:'<gray>Click to open: " + esc(url) + "</gray>'><click:open_url:'" + esc(url) + "'>" + badge + "</click></hover>";
                return PlaceholderResult.value(TextUtil.parseMini(mini));
            }
            return PlaceholderResult.value(TextUtil.parseMini(badge));
        });

        //? if mc26 {
        Placeholders.registerServer(STREAM_ID, (ctx, arg) -> {
        //?} else {
        /*Placeholders.register(STREAM_ID, (ctx, arg) -> {
        *///?}
            UUID target = resolvePlayerUuid(ctx, arg).orElse(null);
            if (target == null || !PlayerDataConfig.isLive(target)) return PlaceholderResult.value(Component.empty());
            String link = PlayerDataConfig.getLink(target);
            return PlaceholderResult.value(link == null || link.isBlank() ? Component.empty() : Component.literal(link));
        });

        //? if mc26 {
        Placeholders.registerServer(LIVE_STREAM_ID, (ctx, arg) -> {
        //?} else {
        /*Placeholders.register(LIVE_STREAM_ID, (ctx, arg) -> {
        *///?}
            UUID target = resolvePlayerUuid(ctx, arg).orElse(null);
            if (target == null || !PlayerDataConfig.isLive(target)) return PlaceholderResult.value(Component.empty());
            Component live = TextUtil.parseMini(LiveManager.placeholder(target));
            String link = PlayerDataConfig.getLink(target);
            if (link == null || link.isBlank()) return PlaceholderResult.value(live);
            return PlaceholderResult.value(live.copy().append(Component.literal(" " + link)));
        });

        //? if mc26 {
        Placeholders.registerServer(LIVE_COUNT_ID, (ctx, arg) -> {
        //?} else {
        /*Placeholders.register(LIVE_COUNT_ID, (ctx, arg) -> {
        *///?}
            if (ctx == null || ctx.player() == null) return PlaceholderResult.value(Component.literal("0"));
            MinecraftServer server = ((ServerLevel) ctx.player().level()).getServer();
            long count = server.getPlayerList().getPlayers().stream()
                .filter(p -> PlayerDataConfig.isLive(p.getUUID()))
                .count();
            return PlaceholderResult.value(Component.literal(String.valueOf(count)));
        });

        //? if mc26 {
        Placeholders.registerServer(CLICKABLE_STREAM_ID, (ctx, arg) -> {
        //?} else {
        /*Placeholders.register(CLICKABLE_STREAM_ID, (ctx, arg) -> {
        *///?}
            UUID target = resolvePlayerUuid(ctx, arg).orElse(null);
            if (target == null || !PlayerDataConfig.isLive(target)) return PlaceholderResult.value(Component.empty());

            String url = TextUtil.normalizeUrl(PlayerDataConfig.getLink(target));
            if (url == null) return PlaceholderResult.value(Component.empty());

            String label = (arg != null && !arg.isBlank()) ? arg.trim() : "Watch Stream";
            String hover = "<gray>Click to open: " + esc(url) + "</gray>";
            String formatted = "<hover:show_text:'" + hover + "'><click:open_url:'" + esc(url) + "'><aqua><underlined>" + esc(label) + "</underlined></aqua></click></hover>";
            return PlaceholderResult.value(TextUtil.parseMini(formatted));
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
        try { return UUID.fromString(s); } catch (IllegalArgumentException ignored) { return null; }
    }

    private static UUID lookupOnlinePlayerUuid(PlaceholderContext ctx, String name) {
        if (ctx == null || ctx.player() == null) return null;
        MinecraftServer server = ((ServerLevel) ctx.player().level()).getServer();
        ServerPlayer target = server.getPlayerList().getPlayerByName(name);
        return target != null ? target.getUUID() : null;
    }

    private static String esc(String s) {
        return s.replace("'", "\\'");
    }
}
