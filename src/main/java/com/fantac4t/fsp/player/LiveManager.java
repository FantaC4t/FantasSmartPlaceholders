package com.fantac4t.fsp.player;

import com.fantac4t.fsp.FSP;
import com.fantac4t.fsp.config.PlayerDataConfig;
import com.fantac4t.fsp.util.Messages;
import com.fantac4t.fsp.util.StreamLinks;
import com.fantac4t.fsp.util.TextUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public final class LiveManager {
    private LiveManager() {}

    public static String placeholder(UUID id) {
        return PlayerDataConfig.isLive(id)
                ? TextUtil.safe(FSP.CONFIG.live.placeholder)
                : TextUtil.safe(FSP.CONFIG.live.offlinePlaceholder);
    }

    public static boolean toggleLive(ServerPlayer player) {
        return setLive(player, !PlayerDataConfig.isLive(player.getUUID()));
    }

    /**
     * Sets live status to an explicit value, announcing only on a real change.
     *
     * <p>The Twitch poller must use this rather than {@link #toggleLive}: it decides off-thread and
     * applies on the server thread, so a blind flip would be applied against whatever the state
     * happens to be on arrival — inverting the wrong way if the player toggled manually in between.
     */
    public static boolean setLive(ServerPlayer player, boolean live) {
        UUID id = player.getUUID();
        if (PlayerDataConfig.isLive(id) == live) return live;
        PlayerDataConfig.setLive(id, live);

        if (live) {
            Messages.send(player, FSP.CONFIG.live.onMessage);
            Messages.broadcast(((ServerLevel) player.level()).getServer(), player, FSP.CONFIG.live.broadcastMessage);
        } else {
            Messages.send(player, FSP.CONFIG.live.offMessage);
        }
        return live;
    }

    public static boolean togglePersist(ServerPlayer player) {
        UUID id = player.getUUID();
        boolean v = !PlayerDataConfig.persist(id);
        PlayerDataConfig.setPersist(id, v);
        Messages.send(player, v ? FSP.CONFIG.live.persistOnMessage : FSP.CONFIG.live.persistOffMessage);
        return v;
    }

    public static void setLink(ServerPlayer player, String link) {
        UUID id = player.getUUID();
        PlayerDataConfig.setLink(id, link);
        String channel = StreamLinks.extractTwitchChannel(link);
        if (channel != null) {
            PlayerDataConfig.setTwitchChannel(id, channel);
        } else {
            PlayerDataConfig.clearTwitchChannel(id);
        }
        Messages.send(player, FSP.CONFIG.live.linkSetMessage);
        if (channel != null && !TwitchManager.isConfigured()) {
            Messages.send(player, FSP.CONFIG.live.twitchNotConfiguredMessage);
        }
    }

    public static void clearLink(ServerPlayer player) {
        PlayerDataConfig.setLink(player.getUUID(), "");
        PlayerDataConfig.clearTwitchChannel(player.getUUID());
    }

}
