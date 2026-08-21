package com.fantac4t.playerstatus.config;

import com.fantac4t.playerstatus.PlayerStatus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerDataConfig {
    private static final Map<UUID, PlayerData> PLAYER_DATA = new ConcurrentHashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File PLAYER_DATA_FILE;
    private static volatile boolean dirty = false;

    public static void load() {
        File dir = new File("config/Fanta's Placeholders");
        if (!dir.exists()) dir.mkdirs();
        PLAYER_DATA_FILE = new File(dir, "player_data.json");
        if (!PLAYER_DATA_FILE.exists()) return;

        try (Reader reader = new FileReader(PLAYER_DATA_FILE)) {
            Type type = new TypeToken<Map<String, PlayerData>>(){}.getType();
            Map<String, PlayerData> loaded = GSON.fromJson(reader, type);
            if (loaded != null) {
                PLAYER_DATA.clear();
                for (Map.Entry<String, PlayerData> e : loaded.entrySet()) {
                    try {
                        PLAYER_DATA.put(UUID.fromString(e.getKey()), e.getValue());
                    } catch (IllegalArgumentException ex) {
                        PlayerStatus.LOGGER.warn("Skipping invalid UUID key: {}", e.getKey());
                    }
                }
            }
        } catch (Exception ex) {
            PlayerStatus.LOGGER.error("Failed to load player_data.json. NOT overwriting — fix JSON manually.", ex);
        }
    }

    private static void markDirty() { dirty = true; }

    public static void save() {
        if (!dirty || PLAYER_DATA_FILE == null) return;
        try (Writer w = new FileWriter(PLAYER_DATA_FILE)) {
            Map<String, PlayerData> out = new HashMap<>();
            for (var e : PLAYER_DATA.entrySet()) {
                out.put(e.getKey().toString(), e.getValue());
            }
            GSON.toJson(out, w);
            dirty = false;
        } catch (Exception e) {
            PlayerStatus.LOGGER.error("Failed to save player data", e);
        }
    }

    private static PlayerData get(UUID id) {
        return PLAYER_DATA.computeIfAbsent(id, k -> new PlayerData());
    }

    // ── Live ────────────────────────────────────────────────────────
    public static boolean isLive(UUID id)              { return get(id).isLive; }
    public static void    setLive(UUID id, boolean v)  { get(id).isLive = v; markDirty(); }

    public static boolean persist(UUID id)             { return get(id).persist; }
    public static void    setPersist(UUID id, boolean v){ get(id).persist = v; markDirty(); }

    public static String  getLink(UUID id)             { return get(id).link; }
    public static void    setLink(UUID id, String v)   { get(id).link = v; markDirty(); }

    // ── Color ───────────────────────────────────────────────────────
    public static String  getColor(UUID id)            { return get(id).color; }
    public static void    setColor(UUID id, String v)  { get(id).color = v; markDirty(); }
    public static void    clearColor(UUID id)          { get(id).color = ""; markDirty(); }

    // ── Nametag ─────────────────────────────────────────────────────
    public static String  getNametag(UUID id)           { return get(id).nametag; }
    public static void    setNametag(UUID id, String v) { get(id).nametag = v; markDirty(); }
    public static void    clearNametag(UUID id)         { get(id).nametag = ""; markDirty(); }

    // ── Twitch ──────────────────────────────────────────────────────
    public static String  getTwitchChannel(UUID id)           { return get(id).twitchChannel; }
    public static void    setTwitchChannel(UUID id, String v) { get(id).twitchChannel = v; markDirty(); }
    public static void    clearTwitchChannel(UUID id)         { get(id).twitchChannel = ""; markDirty(); }

    // ── Lore ────────────────────────────────────────────────────────
    public static String  getLore(UUID id)             { return get(id).lore; }
    public static void    setLore(UUID id, String v)   { get(id).lore = v; markDirty(); }
    public static void    clearLore(UUID id)           { get(id).lore = ""; markDirty(); }

    // ── Stored name (for offline profile lookup) ─────────────────────
    public static String  getStoredName(UUID id)           { return get(id).storedName; }
    public static void    setStoredName(UUID id, String v) { get(id).storedName = v; markDirty(); }

    public static Optional<UUID> findUuidByName(String name) {
        for (Map.Entry<UUID, PlayerData> e : PLAYER_DATA.entrySet()) {
            if (name.equalsIgnoreCase(e.getValue().storedName)) return Optional.of(e.getKey());
        }
        return Optional.empty();
    }

    // ── No-Sleep ────────────────────────────────────────────────────
    public static boolean isNoSleep(UUID id)           { return get(id).noSleep; }
    public static void    setNoSleep(UUID id, boolean v){ get(id).noSleep = v; markDirty(); }

    public static List<UUID> getNoSleepPlayers() {
        List<UUID> result = new ArrayList<>();
        for (Map.Entry<UUID, PlayerData> entry : PLAYER_DATA.entrySet()) {
            if (entry.getValue().noSleep) result.add(entry.getKey());
        }
        return result;
    }

    public static Map<UUID, String> getNametaggedPlayers() {
        Map<UUID, String> result = new LinkedHashMap<>();
        for (Map.Entry<UUID, PlayerData> entry : PLAYER_DATA.entrySet()) {
            String tag = entry.getValue().nametag;
            if (tag != null && !tag.isBlank()) result.put(entry.getKey(), tag);
        }
        return result;
    }

    private static class PlayerData {
        boolean isLive       = false;
        boolean persist      = false;
        String  link         = "";
        String  color        = "";
        @SerializedName("suffix") String nametag = "";
        boolean noSleep      = false;
        String  twitchChannel = "";
        String  lore          = "";
        String  storedName    = "";
    }
}
