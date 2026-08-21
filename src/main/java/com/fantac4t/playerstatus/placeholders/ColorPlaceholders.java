package com.fantac4t.playerstatus.placeholders;

import com.fantac4t.playerstatus.PlayerStatus;
import com.fantac4t.playerstatus.config.PlayerDataConfig;
import com.fantac4t.playerstatus.util.RGBColorProcessor;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class ColorPlaceholders {
    public static void register() {
        // IMPORTANT: Use "coloredname" without underscore to match decompiled version
        //? if mc26 {
        Placeholders.registerServer(Identifier.fromNamespaceAndPath(PlayerStatus.MOD_ID, "coloredname"), (ctx, arg) -> {
        //?} else {
        /*Placeholders.register(Identifier.fromNamespaceAndPath(PlayerStatus.MOD_ID, "coloredname"), (ctx, arg) -> {
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
        Placeholders.registerServer(Identifier.fromNamespaceAndPath(PlayerStatus.MOD_ID, "color"), (ctx, arg) -> {
        //?} else {
        /*Placeholders.register(Identifier.fromNamespaceAndPath(PlayerStatus.MOD_ID, "color"), (ctx, arg) -> {
        *///?}
            if (!ctx.hasPlayer()) {
                return PlaceholderResult.value("");
            }
            String color = PlayerDataConfig.getColor(ctx.player().getUUID());
            return PlaceholderResult.value(color != null ? color : "");
        });
    }
}