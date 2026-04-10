package com.fantac4t.playerstatus.commands;

import com.fantac4t.playerstatus.PlayerStatus;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;

/**
 * Placeholder integration for an optional MoreColor API.
 * Currently a no-op unless the target mod is detected.
 * Adjust mod IDs in MOD_IDS if the real dependency uses different ids.
 */
public final class MoreColorAPIIntegration {

    private static final String[] MOD_IDS = {
            "morecolor",
            "morecolorapi"
    };

    private static final boolean AVAILABLE = detect();

    private MoreColorAPIIntegration() {}

    public static void sync(ServerPlayer player, String color) {
        if (!AVAILABLE || player == null) {
            return;
        }
        // TODO: Replace with actual API calls when available.
        PlayerStatus.LOGGER.debug("[MoreColorAPIIntegration] Sync request for {} color={}", player.getName().getString(), color);
    }

    private static boolean detect() {
        FabricLoader loader = FabricLoader.getInstance();
        for (String id : MOD_IDS) {
            if (loader.isModLoaded(id)) {
                PlayerStatus.LOGGER.info("[MoreColorAPIIntegration] Detected mod '{}', enabling integration stub.", id);
                return true;
            }
        }
        return false;
    }
}