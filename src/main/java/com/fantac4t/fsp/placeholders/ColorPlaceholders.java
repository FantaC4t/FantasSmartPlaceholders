package com.fantac4t.fsp.placeholders;

import com.fantac4t.fsp.FSP;
import com.fantac4t.fsp.config.PlayerDataConfig;
import com.fantac4t.fsp.util.RGBColorProcessor;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class ColorPlaceholders {
    public static void register() {
        // IMPORTANT: Use "coloredname" without underscore to match decompiled version
        //? if mc26 {
        Placeholders.registerServer(Identifier.fromNamespaceAndPath(FSP.PLACEHOLDER_NS, "coloredname"), (ctx, arg) -> {
        //?} else {
        /*Placeholders.register(Identifier.fromNamespaceAndPath(FSP.PLACEHOLDER_NS, "coloredname"), (ctx, arg) -> {
        *///?}
            if (!ctx.hasPlayer()) {
                return PlaceholderResult.value(Component.empty());
            }
            String color = PlayerDataConfig.getColor(ctx.player().getUUID());
            return PlaceholderResult.value(RGBColorProcessor.getColoredPlayerName(
                ctx.player().getName().getString(), color
            ));
        });

        //? if mc26 {
        Placeholders.registerServer(Identifier.fromNamespaceAndPath(FSP.PLACEHOLDER_NS, "color"), (ctx, arg) -> {
        //?} else {
        /*Placeholders.register(Identifier.fromNamespaceAndPath(FSP.PLACEHOLDER_NS, "color"), (ctx, arg) -> {
        *///?}
            if (!ctx.hasPlayer()) {
                return PlaceholderResult.value("");
            }
            String color = PlayerDataConfig.getColor(ctx.player().getUUID());
            return PlaceholderResult.value(color != null ? color : "");
        });
    }
}