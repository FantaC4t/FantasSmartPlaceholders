package com.fantac4t.playerstatus.player;

import com.fantac4t.playerstatus.PlayerStatus;
import com.fantac4t.playerstatus.config.PlayerDataConfig;
import com.fantac4t.playerstatus.twitch.TwitchManager;
import com.fantac4t.playerstatus.util.TextUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LiveManager {
    private LiveManager() {}

    private static final Pattern URL_PATTERN = Pattern.compile(
        "https?://(?:[-\\w.])+(?::\\d+)?(?:/[\\w\\-./_%?&=+#]*)?",
        Pattern.CASE_INSENSITIVE
    );

    public static String placeholder(UUID id) {
        return PlayerDataConfig.isLive(id)
                ? TextUtil.safe(PlayerStatus.CONFIG.livePlaceholder)
                : TextUtil.safe(PlayerStatus.CONFIG.notLivePlaceholder);
    }

    public static boolean toggleLive(ServerPlayer player) {
        UUID id = player.getUUID();
        boolean newState = !PlayerDataConfig.isLive(id);
        PlayerDataConfig.setLive(id, newState);

        if (newState) {
            sendTemplate(player, PlayerStatus.CONFIG.liveOnMessage);
            broadcast(((ServerLevel) player.level()).getServer(), player, PlayerStatus.CONFIG.liveBroadcastMessage);
        } else {
            sendTemplate(player, PlayerStatus.CONFIG.liveOffMessage);
        }
        return newState;
    }

    public static boolean togglePersist(ServerPlayer player) {
        UUID id = player.getUUID();
        boolean v = !PlayerDataConfig.persist(id);
        PlayerDataConfig.setPersist(id, v);
        sendTemplate(player, v ? PlayerStatus.CONFIG.livePersistOnMessage : PlayerStatus.CONFIG.livePersistOffMessage);
        return v;
    }

    public static void setLink(ServerPlayer player, String link) {
        UUID id = player.getUUID();
        PlayerDataConfig.setLink(id, link);
        String channel = extractTwitchChannel(link);
        if (channel != null) {
            PlayerDataConfig.setTwitchChannel(id, channel);
        } else {
            PlayerDataConfig.clearTwitchChannel(id);
        }
        sendTemplate(player, PlayerStatus.CONFIG.liveLinkSetMessage);
        if (channel != null && !TwitchManager.isConfigured()) {
            sendTemplate(player, PlayerStatus.CONFIG.liveLinkTwitchNotConfiguredMessage);
        }
    }

    public static void clearLink(ServerPlayer player) {
        PlayerDataConfig.setLink(player.getUUID(), "");
        PlayerDataConfig.clearTwitchChannel(player.getUUID());
    }

    private static String extractTwitchChannel(String url) {
        if (url == null || url.isBlank()) return null;
        int idx = url.toLowerCase().indexOf("twitch.tv/");
        if (idx < 0) return null;
        String rest = url.substring(idx + "twitch.tv/".length()).toLowerCase();
        int slash = rest.indexOf('/');
        if (slash >= 0) rest = rest.substring(0, slash);
        int query = rest.indexOf('?');
        if (query >= 0) rest = rest.substring(0, query);
        return rest.isBlank() ? null : rest;
    }

    private static void broadcast(MinecraftServer server, ServerPlayer source, String template) {
        if (server == null || TextUtil.empty(template)) return;
        Component comp = buildMessage(template, source);
        server.getPlayerList().getPlayers().forEach(p -> p.sendSystemMessage(comp));
    }

    private static void sendTemplate(ServerPlayer player, String template) {
        if (TextUtil.empty(template)) return;
        player.sendSystemMessage(buildMessage(template, player));
    }

    private static Component buildMessage(String template, ServerPlayer player) {
        if (TextUtil.empty(template)) return Component.empty();
        try {
            String expanded = expandCustomPlaceholders(template, player);
            String tagged = ensureClickableUrlTags(expanded);
            return TextUtil.parseMini(tagged, player);
        } catch (Throwable t) {
            PlayerStatus.LOGGER.warn("[PlayerStatus] Message building failed: {}", t.toString());
            return Component.literal(template);
        }
    }

    private static String ensureClickableUrlTags(String input) {
        if (TextUtil.empty(input)) return "";
        if (input.contains("<click:")) return input;

        StringBuilder sb = new StringBuilder();
        Matcher m = URL_PATTERN.matcher(input);
        int last = 0;

        while (m.find()) {
            if (m.start() > last) sb.append(input, last, m.start());
            String url = input.substring(m.start(), m.end());
            sb.append("<hover:show_text:'<gray>Click to open: ").append(url).append("</gray>'>")
              .append("<click:open_url:'").append(url).append("'>")
              .append("<aqua><underlined>").append(url).append("</underlined></aqua>")
              .append("</click></hover>");
            last = m.end();
        }

        if (last < input.length()) sb.append(input, last, input.length());
        return sb.toString();
    }

    private static String expandCustomPlaceholders(String template, ServerPlayer player) {
        String name = player.getName().getString();
        String link = normalizeUrl(TextUtil.safe(PlayerDataConfig.getLink(player.getUUID())));
        return template
                .replace("{player}",      name)
                .replace("%player_name%", name)
                .replace("{link}",        link)
                .replace("%link%",        link);
    }

    private static String normalizeUrl(String s) {
        if (s == null || s.isBlank()) return "";
        if (!s.matches("(?i)https?://.*")) return "https://" + s;
        return s;
    }
}
