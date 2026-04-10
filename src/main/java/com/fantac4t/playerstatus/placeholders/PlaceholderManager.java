package com.fantac4t.playerstatus.placeholders;

import com.fantac4t.playerstatus.PlayerStatus;
import com.fantac4t.playerstatus.config.PlayerDataConfig;
import com.fantac4t.playerstatus.player.LiveManager;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public final class PlaceholderManager {

    public static void register() {
        // Live status text
        Placeholders.register(id("live"), (ctx, arg) -> {
            if (!ctx.hasPlayer()) return PlaceholderResult.value("");
            return PlaceholderResult.value(LiveManager.placeholder(ctx.player().getUUID()));
        });

        // Raw stream URL (plain)
        Placeholders.register(id("stream"), (ctx, arg) -> {
            if (!ctx.hasPlayer()) return PlaceholderResult.value("");
            String link = PlayerDataConfig.getLink(ctx.player().getUUID());
            return PlaceholderResult.value(link == null ? "" : link);
        });

        // Clickable stream link (currently returns styled text only; click added later by linkifier)
        // If you have a post-parser linkification, this will still become clickable.
        Placeholders.register(id("clickable_stream"), (ctx, arg) -> {
            if (!ctx.hasPlayer()) return PlaceholderResult.value("");
            UUID id = ctx.player().getUUID();
            String url = PlayerDataConfig.getLink(id);
            if (url == null || url.isBlank()) return PlaceholderResult.value("");
            // Just color + underline (no click event to avoid abstract constructor issues)
            return PlaceholderResult.value(
                    Component.literal(url)
                            .withStyle(s -> s.withColor(ChatFormatting.AQUA).withUnderlined(true))
            );
        });

        // Colored name (hex tag returned so your later parser can process)
        Placeholders.register(id("coloredname"), (ctx, arg) -> {
            if (!ctx.hasPlayer()) return PlaceholderResult.value("");
            String base = ctx.player().getName().getString();
            String hex = PlayerDataConfig.getColor(ctx.player().getUUID());
            if (hex != null && hex.matches("^#?[0-9a-fA-F]{6}$")) {
                if (!hex.startsWith("#")) hex = "#" + hex;
                return PlaceholderResult.value("<" + hex + ">" + base + "</>");
            }
            return PlaceholderResult.value(base);
        });
    }

    private static Identifier id(String path) {
        // Constructor is private in official mappings
        return Identifier.fromNamespaceAndPath(PlayerStatus.MOD_ID, path);
    }

    private PlaceholderManager() {}
}