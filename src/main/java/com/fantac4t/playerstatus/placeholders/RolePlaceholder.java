package com.fantac4t.playerstatus.placeholders;

import com.fantac4t.playerstatus.PlayerStatus;
import com.fantac4t.playerstatus.config.PlayerDataConfig;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public final class RolePlaceholder {

    private static final Identifier ROLE_ID =
        Identifier.fromNamespaceAndPath(PlayerStatus.MOD_ID, "role");
    private static final Identifier SUFFIX_ID =
        Identifier.fromNamespaceAndPath(PlayerStatus.MOD_ID, "suffix");

    public static void register() {
        Placeholders.register(ROLE_ID, (ctx, arg) -> result(ctx));
        Placeholders.register(SUFFIX_ID, (ctx, arg) -> result(ctx));
    }

    private static PlaceholderResult result(eu.pb4.placeholders.api.PlaceholderContext ctx) {
        if (ctx == null || !ctx.hasPlayer()) {
            return PlaceholderResult.value("");
        }
        UUID id = ctx.player().getUUID();
        String value = PlayerDataConfig.getSuffix(id);
        return PlaceholderResult.value(value != null ? value : "");
    }
}