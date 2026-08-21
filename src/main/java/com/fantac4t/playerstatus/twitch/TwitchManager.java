package com.fantac4t.playerstatus.twitch;

import com.fantac4t.playerstatus.PlayerStatus;
import com.fantac4t.playerstatus.config.PlayerDataConfig;
import com.fantac4t.playerstatus.player.LiveManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class TwitchManager {
    private TwitchManager() {}

    private static final HttpClient HTTP = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    private static volatile String accessToken = null;
    private static volatile Instant tokenExpiry = Instant.EPOCH;

    private static ScheduledExecutorService scheduler;
    private static volatile MinecraftServer SERVER;

    /** Whether the server admin has set twitchClientId/twitchClientSecret in config.json, enabling the live poller. */
    public static boolean isConfigured() {
        return !PlayerStatus.CONFIG.twitchClientId.isBlank() && !PlayerStatus.CONFIG.twitchClientSecret.isBlank();
    }

    public static void start(MinecraftServer server) {
        if (!isConfigured()) {
            return;
        }
        SERVER = server;
        int interval = Math.max(30, PlayerStatus.CONFIG.twitchPollIntervalSeconds);
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "playerstatus-twitch");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(TwitchManager::poll, 10, interval, TimeUnit.SECONDS);
        PlayerStatus.LOGGER.info("[PlayerStatus] Twitch poller started (interval: {}s)", interval);
    }

    public static void stop() {
        SERVER = null;
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    private static void poll() {
        try {
            MinecraftServer srv = SERVER;
            if (srv == null) return;
            if (!refreshTokenIfNeeded()) return;

            List<ServerPlayer> players = srv.getPlayerList().getPlayers();
            Map<String, UUID> channelToUuid = new LinkedHashMap<>();
            for (ServerPlayer p : players) {
                String ch = PlayerDataConfig.getTwitchChannel(p.getUUID());
                if (ch != null && !ch.isBlank()) channelToUuid.put(ch.toLowerCase(), p.getUUID());
            }
            if (channelToUuid.isEmpty()) return;

            StringBuilder url = new StringBuilder("https://api.twitch.tv/helix/streams?first=100");
            for (String ch : channelToUuid.keySet()) {
                url.append("&user_login=").append(URLEncoder.encode(ch, StandardCharsets.UTF_8));
            }

            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url.toString()))
                .header("Authorization", "Bearer " + accessToken)
                .header("Client-Id", PlayerStatus.CONFIG.twitchClientId)
                .timeout(Duration.ofSeconds(10))
                .GET().build();

            String body = HTTP.send(req, HttpResponse.BodyHandlers.ofString()).body();
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();

            Set<String> nowLive = new LinkedHashSet<>();
            for (var el : json.getAsJsonArray("data")) {
                nowLive.add(el.getAsJsonObject().get("user_login").getAsString().toLowerCase());
            }

            for (Map.Entry<String, UUID> entry : channelToUuid.entrySet()) {
                String channel  = entry.getKey();
                UUID   uuid     = entry.getValue();
                boolean wasLive   = PlayerDataConfig.isLive(uuid);
                boolean isNowLive = nowLive.contains(channel);

                if (isNowLive && !wasLive) {
                    srv.execute(() -> {
                        ServerPlayer p = srv.getPlayerList().getPlayer(uuid);
                        if (p != null) LiveManager.toggleLive(p);
                    });
                } else if (!isNowLive && wasLive) {
                    srv.execute(() -> {
                        ServerPlayer p = srv.getPlayerList().getPlayer(uuid);
                        if (p != null) LiveManager.toggleLive(p);
                    });
                }
            }
        } catch (Exception e) {
            PlayerStatus.LOGGER.warn("[PlayerStatus] Twitch poll failed: {}", e.getMessage());
        }
    }

    private static boolean refreshTokenIfNeeded() throws Exception {
        if (accessToken != null && Instant.now().isBefore(tokenExpiry)) return true;

        String form = "client_id=" + URLEncoder.encode(PlayerStatus.CONFIG.twitchClientId, StandardCharsets.UTF_8)
            + "&client_secret=" + URLEncoder.encode(PlayerStatus.CONFIG.twitchClientSecret, StandardCharsets.UTF_8)
            + "&grant_type=client_credentials";

        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create("https://id.twitch.tv/oauth2/token"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(form))
            .timeout(Duration.ofSeconds(10))
            .build();

        String body = HTTP.send(req, HttpResponse.BodyHandlers.ofString()).body();
        JsonObject json = JsonParser.parseString(body).getAsJsonObject();
        if (!json.has("access_token")) {
            PlayerStatus.LOGGER.error("[PlayerStatus] Twitch token refresh failed: {}", body);
            return false;
        }
        accessToken = json.get("access_token").getAsString();
        int expiresIn = json.get("expires_in").getAsInt();
        tokenExpiry = Instant.now().plusSeconds(expiresIn - 300);
        PlayerStatus.LOGGER.info("[PlayerStatus] Twitch access token refreshed (expires in {}s)", expiresIn);
        return true;
    }
}
