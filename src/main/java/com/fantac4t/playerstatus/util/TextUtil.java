package com.fantac4t.playerstatus.util;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class TextUtil {
    private TextUtil() {}

    public static boolean empty(String s) { return s == null || s.isEmpty(); }
    public static String  safe(String s)  { return s == null ? "" : s; }

    public static Component parseMini(String text) {
        return parseMini(text, null);
    }

    public static Component parseMini(String text, ServerPlayer player) {
        if (empty(text)) return Component.empty();
        try {
            Component parsed = TextParserUtils.formatText(text);
            if (player != null) {
                return Placeholders.parseText(parsed, PlaceholderContext.of(player));
            }
            return parsed;
        } catch (Throwable t) {
            return Component.literal(text);
        }
    }
}
