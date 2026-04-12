package com.fantac4t.playerstatus.events;

import com.fantac4t.playerstatus.config.PlayerDataConfig;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class PlayerEvents {
    public static void register() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            var player = handler.getPlayer();
            var id = player.getUUID();
            // Reset live status if not persistent
            if (!PlayerDataConfig.persist(id) && PlayerDataConfig.isLive(id)) {
                PlayerDataConfig.setLive(id, false);
            }
            // Always reset noSleep status on disconnect
            if (PlayerDataConfig.isNoSleep(id)) {
                PlayerDataConfig.setNoSleep(id, false);
            }
        });
    }
}