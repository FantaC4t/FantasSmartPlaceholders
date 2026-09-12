package com.fantac4t.fsp.player;

import com.fantac4t.fsp.config.PlayerDataConfig;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class PlayerEvents {
    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var player = handler.getPlayer();
            PlayerDataConfig.setStoredName(player.getUUID(), player.getName().getString());
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            var id = handler.getPlayer().getUUID();

            if (!PlayerDataConfig.persist(id) && PlayerDataConfig.isLive(id)) {
                PlayerDataConfig.setLive(id, false);
            }
            if (PlayerDataConfig.isNoSleep(id)) {
                PlayerDataConfig.setNoSleep(id, false);
            }

            // Save on every disconnect so a crash doesn't lose data
            PlayerDataConfig.save();
        });
    }
}
