package com.fantac4t.playerstatus.util;

import eu.pb4.placeholders.api.parsers.TagParser;
import net.minecraft.network.chat.Component;

public final class TextUtil {
    private TextUtil() {}

    public static boolean empty(String s) { return s == null || s.isEmpty(); }
    public static String  safe(String s)  { return s == null ? "" : s; }

    public static Component parseMini(String text) {
        if (empty(text)) return Component.empty();
        try {
            return TagParser.DEFAULT.parseText(text, null);
        } catch (Throwable t) {
            return Component.literal(text);
        }
    }
}
