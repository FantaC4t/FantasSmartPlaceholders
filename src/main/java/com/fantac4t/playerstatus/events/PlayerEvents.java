package com.fantac4t.playerstatus.events;

import com.fantac4t.playerstatus.config.PlayerDataConfig;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class PlayerEvents {
    public static void register() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            var player = handler.getPlayer();
            var id = player.getUUID();
            if (!PlayerDataConfig.persist(id) && PlayerDataConfig.isLive(id)) {
                PlayerDataConfig.setLive(id, false);
            }
        });
    }
}