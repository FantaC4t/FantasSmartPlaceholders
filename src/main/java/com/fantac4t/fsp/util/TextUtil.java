package com.fantac4t.fsp.util;

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

    //? if mc26 {
    // Built once: NodeParser.merge allocates a new merged parser per call, and parseMini runs on
    // every placeholder evaluation, for every player, on every tab-list/scoreboard refresh.
    private static final NodeParser MINI_PARSER =
        NodeParser.merge(TagParser.SIMPLIFIED_TEXT_FORMAT, Placeholders.SERVER_PLACEHOLDER_PARSER);
    //?}

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
            return MINI_PARSER.parseComponent(text, ctx);
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

    /**
     * Parses player-supplied text (lore, etc.). Colour, gradient and formatting tags work,
     * but interactive tags (click / hover / insertion / run_command / nbt / selector …) are
     * dropped, and no placeholders are resolved. Use this for anything a player can type that
     * another player will see rendered.
     */
    public static Component parseUserMini(String text) {
        if (empty(text)) return Component.empty();
        try {
            //? if mc26 {
            return TagParser.SIMPLIFIED_TEXT_FORMAT_SAFE.parseComponent(text, ParserContext.of());
            //?} else {
            /*return TextParserUtils.formatTextSafe(text);
            *///?}
        } catch (Throwable t) {
            return Component.literal(text);
        }
    }

    private static final Pattern SAFE_URL = Pattern.compile(
        "(?i)^(?:https?://)?[a-z0-9](?:[a-z0-9-]*[a-z0-9])?(?:\\.[a-z0-9](?:[a-z0-9-]*[a-z0-9])?)+"
            + "(?::\\d{1,5})?(?:/[a-z0-9\\-._~:/?#\\[\\]@!$&()*+,;=%]*)?$");

    /** True if {@code s} is a plausible http(s) URL with no characters that could break out of a MiniMessage tag. */
    public static boolean looksLikeUrl(String s) {
        return s != null && SAFE_URL.matcher(s.trim()).matches();
    }

    /**
     * Strips the characters that would let embedded text break out of a MiniMessage tag.
     *
     * <p>Angle brackets would open a new tag; a single quote would close the quoted argument of one
     * (as in {@code <click:open_url:'…'>}). All three are removed rather than backslash-escaped so
     * the result is safe in both quoted and unquoted positions — an escape that is correct inside
     * {@code '…'} renders as a literal backslash outside it.
     */
    public static String escapeMini(String s) {
        if (s == null) return "";
        return s.replace("<", "").replace(">", "").replace("'", "");
    }
}
