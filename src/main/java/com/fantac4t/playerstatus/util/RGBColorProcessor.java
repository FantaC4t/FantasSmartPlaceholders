package com.fantac4t.playerstatus.util;

import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public final class RGBColorProcessor {
    private static final Pattern HEX_PATTERN = Pattern.compile("#[0-9a-fA-F]{6}");
    private static final Pattern HEX_NO_HASH_PATTERN = Pattern.compile("[0-9a-fA-F]{6}");
    
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
        
        // Legacy color code (like "c" for red)
        return input.length() <= 3;
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
    
    public static String hexToLegacyCode(String hexColor) {
        hexColor = normalizeColorInput(hexColor);
        
        if (hexColor == null || !HEX_PATTERN.matcher(hexColor).matches()) {
            return "§f";
        }
        
        String hex = hexColor.substring(1);
        StringBuilder legacyCode = new StringBuilder("§x");
        
        for (char c : hex.toCharArray()) {
            legacyCode.append("§").append(c);
        }
        
        return legacyCode.toString();
    }
    
    public static Component getColoredPlayerName(String name, String hexColor) {
        if (hexColor == null || hexColor.isEmpty()) {
            return Component.literal(name);
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
            return Component.literal("§" + hexColor + name);
        }
    }
}