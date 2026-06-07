package com.fantac4t.playerstatus.placeholders;

import com.fantac4t.playerstatus.PlayerStatus;
import com.fantac4t.playerstatus.config.PlayerDataConfig;
import com.fantac4t.playerstatus.util.TextUtil;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import net.fabricmc.loader.api.FabricLoader;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public final class RolePlaceholder {

    private static final Identifier ROLE_ID    = Identifier.fromNamespaceAndPath(PlayerStatus.MOD_ID, "role");
    private static final Identifier NAMETAG_ID = Identifier.fromNamespaceAndPath(PlayerStatus.MOD_ID, "suffix");

    public static void register() {
        Placeholders.register(ROLE_ID,    (ctx, arg) -> roleResult(ctx));
        Placeholders.register(NAMETAG_ID, (ctx, arg) -> nametagResult(ctx));
    }

    private static PlaceholderResult roleResult(PlaceholderContext ctx) {
        if (ctx == null || !ctx.hasPlayer()) return PlaceholderResult.value("");

        String group = resolveGroup(ctx.player().getUUID());
        String symbol = PlayerStatus.CONFIG.roles.getOrDefault(group, "");
        return PlaceholderResult.value(TextUtil.parseMini(symbol));
    }

    private static PlaceholderResult nametagResult(PlaceholderContext ctx) {
        if (ctx == null || !ctx.hasPlayer()) return PlaceholderResult.value("");
        String value = PlayerDataConfig.getNametag(ctx.player().getUUID());
        return PlaceholderResult.value(value != null ? value : "");
    }

    private static String resolveGroup(UUID id) {
        if (!FabricLoader.getInstance().isModLoaded("luckperms")) return "";
        try {
            User user = LuckPermsProvider.get().getUserManager().getUser(id);
            return user != null ? user.getPrimaryGroup() : "";
        } catch (Exception e) {
            PlayerStatus.LOGGER.warn("Failed to resolve LuckPerms group for {}: {}", id, e.getMessage());
            return "";
        }
    }
}
