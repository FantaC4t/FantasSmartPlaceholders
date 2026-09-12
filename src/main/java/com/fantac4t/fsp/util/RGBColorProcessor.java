package com.fantac4t.fsp.util;

import java.util.regex.Pattern;
// TextUtil is in the same package — no import needed
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public final class RGBColorProcessor {
    private static final Pattern HEX_PATTERN         = Pattern.compile("#[0-9a-fA-F]{6}");
    private static final Pattern HEX_NO_HASH_PATTERN = Pattern.compile("[0-9a-fA-F]{6}");
    private static final Pattern GRADIENT_PATTERN    = Pattern.compile("gradient:(#[0-9a-fA-F]{6}):(#[0-9a-fA-F]{6})");
    // A single legacy *colour* code (0-9, a-f). Formatting codes (k-o bold/italic/etc., r reset)
    // are intentionally excluded — /color sets a colour, not a text style.
    private static final Pattern LEGACY_CODE_PATTERN = Pattern.compile("[0-9a-fA-F]");

    public static boolean isValid(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        if (input.startsWith("#")) {
            return HEX_PATTERN.matcher(input).matches();
        } else if (HEX_NO_HASH_PATTERN.matcher(input).matches()) {
            // Valid hex code without the # prefix
            return true;
        }

        // Legacy color code — must be a single valid code char (like "c" for red), not arbitrary text.
        return LEGACY_CODE_PATTERN.matcher(input).matches();
    }

    // Normalize the input by ensuring it has a # prefix if it's a hex code
    public static String normalizeColorInput(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        if (!input.startsWith("#") && HEX_NO_HASH_PATTERN.matcher(input).matches()) {
            return "#" + input;
        }

        return input;
    }

    public static boolean isGradient(String input) {
        return input != null && GRADIENT_PATTERN.matcher(input).matches();
    }

    public static Component getColoredPlayerName(String name, String hexColor) {
        if (hexColor == null || hexColor.isEmpty()) {
            return Component.literal(name);
        }

        if (isGradient(hexColor)) {
            // stored as "gradient:#RRGGBB:#RRGGBB"
            String[] parts = hexColor.split(":", 3);
            String mini = "<gradient:" + parts[1] + ":" + parts[2] + ">" + name + "</gradient>";
            return TextUtil.parseMini(mini);
        }

        // Normalize the color input
        hexColor = normalizeColorInput(hexColor);

        if (hexColor.startsWith("#")) {
            if (!HEX_PATTERN.matcher(hexColor).matches()) {
                return Component.literal(name);
            }

            try {
                int rgb = Integer.parseInt(hexColor.substring(1), 16);
                return Component.literal(name).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)));
            } catch (NumberFormatException e) {
                return Component.literal(name);
            }
        } else {
            // Legacy single-char code: apply it as a real Style instead of injecting a raw § sequence.
            ChatFormatting fmt = hexColor.length() == 1
                ? ChatFormatting.getByCode(Character.toLowerCase(hexColor.charAt(0)))
                : null;
            if (fmt != null) {
                return Component.literal(name).withStyle(fmt);
            }
            return Component.literal(name);
        }
    }
}