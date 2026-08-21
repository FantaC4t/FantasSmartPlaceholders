package com.fantac4t.playerstatus.util;

//? if mc26 {
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.ServerPlaceholderContext;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.TagParser;
//?} else {
/*import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
*///?}
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.net.URI;
import java.util.regex.Pattern;

public final class TextUtil {
    private TextUtil() {}

    private static final Pattern HTTP_URL = Pattern.compile("(?i)^https?://.+");

    public static boolean empty(String s) { return s == null || s.isEmpty(); }
    public static String  safe(String s)  { return s == null ? "" : s; }

    /** Normalizes a user-supplied link to a valid https?:// URL, or null if it can't be made into one. */
    public static String normalizeUrl(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        if (!HTTP_URL.matcher(t).matches()) t = "https://" + t;
        try { URI.create(t); return t; } catch (Exception e) { return null; }
    }

    public static Component parseMini(String text) {
        return parseMini(text, null);
    }

    public static Component parseMini(String text, ServerPlayer player) {
        if (empty(text)) return Component.empty();
        try {
            //? if mc26 {
            ParserContext ctx = player != null
                ? ParserContext.of(ServerPlaceholderContext.SERVER_KEY, ServerPlaceholderContext.of(player))
                : ParserContext.of();
            return NodeParser.merge(TagParser.SIMPLIFIED_TEXT_FORMAT, Placeholders.SERVER_PLACEHOLDER_PARSER)
                .parseComponent(text, ctx);
            //?} else {
            /*Component parsed = TextParserUtils.formatText(text);
            if (player != null) {
                return Placeholders.parseText(parsed, PlaceholderContext.of(player));
            }
            return parsed;
            *///?}
        } catch (Throwable t) {
            return Component.literal(text);
        }
    }
}
