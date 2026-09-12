package com.fantac4t.fsp.player;

import com.fantac4t.fsp.FSP;
import com.fantac4t.fsp.config.PlayerDataConfig;
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

    /** Whether the server admin has set twitch.clientId/twitch.clientSecret in config.toml, enabling the live poller. */
    public static boolean isConfigured() {
        return !FSP.CONFIG.twitch.clientId.isBlank() && !FSP.CONFIG.twitch.clientSecret.isBlank();
    }

    public static void start(MinecraftServer server) {
        stop(); // idempotent: safe to call after a config reload without leaking schedulers

        if (!isConfigured()) {
            FSP.LOGGER.info("[FSP] Twitch poller not started — twitch.clientId/twitch.clientSecret not set in config.toml.");
            return;
        }
        SERVER = server;
        int interval = Math.max(30, FSP.CONFIG.twitch.pollIntervalSeconds);
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "fsp-twitch");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(TwitchManager::poll, 10, interval, TimeUnit.SECONDS);
        FSP.LOGGER.info("[FSP] Twitch poller started (interval: {}s)", interval);
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
                .header("Client-Id", FSP.CONFIG.twitch.clientId)
                .timeout(Duration.ofSeconds(10))
                .GET().build();

            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            String body = resp.body();
            if (resp.statusCode() != 200) {
                FSP.LOGGER.warn("[FSP] Twitch /streams returned HTTP {} for channels {}: {}",
                    resp.statusCode(), channelToUuid.keySet(), body);
                return;
            }

            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            if (!json.has("data")) {
                FSP.LOGGER.warn("[FSP] Twitch /streams response had no 'data' field for channels {}: {}",
                    channelToUuid.keySet(), body);
                return;
            }

            Set<String> nowLive = new LinkedHashSet<>();
            for (var el : json.getAsJsonArray("data")) {
                nowLive.add(el.getAsJsonObject().get("user_login").getAsString().toLowerCase());
            }

            FSP.LOGGER.info("[FSP] Twitch poll: checked {} -> live: {}", channelToUuid.keySet(), nowLive);

            for (Map.Entry<String, UUID> entry : channelToUuid.entrySet()) {
                String channel  = entry.getKey();
                UUID   uuid     = entry.getValue();
                boolean isNowLive = nowLive.contains(channel);

                // Twitch is the source of truth for a player who linked a channel, so hand
                // setLive the absolute state rather than a flip — by the time this runs on the
                // server thread the stored value may have moved (a manual /live, say), and a flip
                // would then invert the wrong way. setLive re-checks and no-ops if it already matches.
                if (isNowLive != PlayerDataConfig.isLive(uuid)) {
                    srv.execute(() -> {
                        ServerPlayer p = srv.getPlayerList().getPlayer(uuid);
                        if (p != null) LiveManager.setLive(p, isNowLive);
                    });
                }
            }
        } catch (Exception e) {
            FSP.LOGGER.warn("[FSP] Twitch poll failed: {}", e.toString());
        }
    }

    private static boolean refreshTokenIfNeeded() throws Exception {
        if (accessToken != null && Instant.now().isBefore(tokenExpiry)) return true;

        String form = "client_id=" + URLEncoder.encode(FSP.CONFIG.twitch.clientId, StandardCharsets.UTF_8)
            + "&client_secret=" + URLEncoder.encode(FSP.CONFIG.twitch.clientSecret, StandardCharsets.UTF_8)
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
            FSP.LOGGER.error("[FSP] Twitch token refresh failed: {}", body);
            return false;
        }
        accessToken = json.get("access_token").getAsString();
        int expiresIn = json.get("expires_in").getAsInt();
        tokenExpiry = Instant.now().plusSeconds(expiresIn - 300);
        FSP.LOGGER.info("[FSP] Twitch access token refreshed (expires in {}s)", expiresIn);
        return true;
    }
}
