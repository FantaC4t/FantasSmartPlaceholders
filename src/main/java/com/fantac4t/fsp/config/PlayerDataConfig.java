package com.fantac4t.fsp.config;

import com.fantac4t.fsp.FSP;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class PlayerDataConfig {
    private static final Map<UUID, PlayerData> PLAYER_DATA = new ConcurrentHashMap<>();
    // disableHtmlEscaping: lore and nametags hold MiniMessage, and Gson escapes < and > by default,
    // which would store every tag as an unreadable backslash-u escape sequence.
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static File PLAYER_DATA_FILE;
    private static volatile boolean dirty = false;

    /** Serializes saves — the autosave thread and the server thread both call {@link #save()}. */
    private static final Object SAVE_LOCK = new Object();

    private static ScheduledExecutorService autosave;

    public static void load() {
        File dir = new File("config/Fanta's Placeholders");
        if (!dir.exists()) dir.mkdirs();
        PLAYER_DATA_FILE = new File(dir, "player_data.json");
        if (!PLAYER_DATA_FILE.exists()) return;

        try (Reader reader = Files.newBufferedReader(PLAYER_DATA_FILE.toPath(), StandardCharsets.UTF_8)) {
            Type type = new TypeToken<Map<String, PlayerData>>(){}.getType();
            Map<String, PlayerData> loaded = GSON.fromJson(reader, type);
            if (loaded != null) {
                PLAYER_DATA.clear();
                for (Map.Entry<String, PlayerData> e : loaded.entrySet()) {
                    try {
                        PLAYER_DATA.put(UUID.fromString(e.getKey()), e.getValue());
                    } catch (IllegalArgumentException ex) {
                        FSP.LOGGER.warn("Skipping invalid UUID key: {}", e.getKey());
                    }
                }
            }
        } catch (Exception ex) {
            FSP.LOGGER.error("Failed to load player_data.json. NOT overwriting — fix JSON manually.", ex);
        }
    }

    private static void markDirty() { dirty = true; }

    /**
     * Starts flushing player data periodically. Without this, data is only written on disconnect and
     * clean shutdown, so a crash with players online loses everything since the last disconnect.
     */
    public static void startAutosave() {
        stopAutosave();

        int minutes = FSP.CONFIG.storage.autosaveIntervalMinutes;
        if (minutes <= 0) {
            FSP.LOGGER.info("[FSP] Player-data autosave disabled (storage.autosaveIntervalMinutes = {}).", minutes);
            return;
        }

        autosave = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "fsp-autosave");
            t.setDaemon(true);
            return t;
        });
        // save() no-ops unless something changed, so an idle server does no I/O.
        autosave.scheduleAtFixedRate(PlayerDataConfig::save, minutes, minutes, TimeUnit.MINUTES);
        FSP.LOGGER.info("[FSP] Player-data autosave started (every {} minute(s)).", minutes);
    }

    public static void stopAutosave() {
        if (autosave != null) {
            autosave.shutdownNow();
            autosave = null;
        }
    }

    public static void save() {
        if (!dirty || PLAYER_DATA_FILE == null) return;
        synchronized (SAVE_LOCK) {
            // Cleared up front so writes landing mid-save re-mark the data dirty instead of being
            // swallowed by a dirty = false at the end.
            dirty = false;
            try {
                Map<String, PlayerData> out = new HashMap<>();
                for (var e : PLAYER_DATA.entrySet()) {
                    out.put(e.getKey().toString(), e.getValue());
                }

                // Write to a temp file and swap it in, so a crash or full disk mid-write can't leave
                // a truncated player_data.json behind (load() refuses to overwrite a broken file,
                // which would mean silently starting up with everyone's data gone).
                Path target = PLAYER_DATA_FILE.toPath();
                Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
                try (Writer w = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
                    GSON.toJson(out, w);
                }
                try {
                    Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception e) {
                dirty = true; // keep the data pending so the next save retries it
                FSP.LOGGER.error("Failed to save player data", e);
            }
        }
    }

    /** Shared stand-in for players with no stored data. Never mutated — {@link #write} owns all changes. */
    private static final PlayerData EMPTY = new PlayerData();

    /**
     * Read-only view of a player's data. Unlike {@link #write} this does NOT insert an entry,
     * so querying an unknown UUID (placeholders accept an arbitrary one as an argument) can't
     * grow the map or add empty records to player_data.json.
     */
    private static PlayerData read(UUID id) {
        if (id == null) return EMPTY;
        PlayerData data = PLAYER_DATA.get(id);
        return data != null ? data : EMPTY;
    }

    /** Mutable entry for a player, created on first write. */
    private static PlayerData write(UUID id) {
        return PLAYER_DATA.computeIfAbsent(id, k -> new PlayerData());
    }

    // ── Live ────────────────────────────────────────────────────────
    public static boolean isLive(UUID id)              { return read(id).isLive; }
    public static void    setLive(UUID id, boolean v)  { write(id).isLive = v; markDirty(); }

    public static boolean persist(UUID id)             { return read(id).persist; }
    public static void    setPersist(UUID id, boolean v){ write(id).persist = v; markDirty(); }

    public static String  getLink(UUID id)             { return read(id).link; }
    public static void    setLink(UUID id, String v)   { write(id).link = v; markDirty(); }

    // ── Color ───────────────────────────────────────────────────────
    public static String  getColor(UUID id)            { return read(id).color; }
    public static void    setColor(UUID id, String v)  { write(id).color = v; markDirty(); }
    public static void    clearColor(UUID id)          { write(id).color = ""; markDirty(); }

    // ── Nametag ─────────────────────────────────────────────────────
    public static String  getNametag(UUID id)           { return read(id).nametag; }
    public static void    setNametag(UUID id, String v) { write(id).nametag = v; markDirty(); }
    public static void    clearNametag(UUID id)         { write(id).nametag = ""; markDirty(); }

    // ── Twitch ──────────────────────────────────────────────────────
    public static String  getTwitchChannel(UUID id)           { return read(id).twitchChannel; }
    public static void    setTwitchChannel(UUID id, String v) { write(id).twitchChannel = v; markDirty(); }
    public static void    clearTwitchChannel(UUID id)         { write(id).twitchChannel = ""; markDirty(); }

    // ── Lore ────────────────────────────────────────────────────────
    public static String  getLore(UUID id)             { return read(id).lore; }
    public static void    setLore(UUID id, String v)   { write(id).lore = v; markDirty(); }
    public static void    clearLore(UUID id)           { write(id).lore = ""; markDirty(); }

    // ── Stored name (for offline profile lookup) ─────────────────────
    public static String  getStoredName(UUID id)           { return read(id).storedName; }
    public static void    setStoredName(UUID id, String v) { write(id).storedName = v; markDirty(); }

    public static Optional<UUID> findUuidByName(String name) {
        for (Map.Entry<UUID, PlayerData> e : PLAYER_DATA.entrySet()) {
            if (name.equalsIgnoreCase(e.getValue().storedName)) return Optional.of(e.getKey());
        }
        return Optional.empty();
    }

    /** Every player name we've seen before, for suggesting offline players in command completions. */
    public static List<String> knownNames() {
        List<String> names = new ArrayList<>();
        for (PlayerData data : PLAYER_DATA.values()) {
            if (data.storedName != null && !data.storedName.isBlank()) names.add(data.storedName);
        }
        return names;
    }

    // ── No-Sleep ────────────────────────────────────────────────────
    public static boolean isNoSleep(UUID id)           { return read(id).noSleep; }
    public static void    setNoSleep(UUID id, boolean v){ write(id).noSleep = v; markDirty(); }

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

    /**
     * Fields are volatile because the Twitch poller reads isLive/twitchChannel from its own thread
     * while the server thread writes them. ConcurrentHashMap only publishes the entry itself, not
     * field writes made after it was inserted, so without this the poller can see a stale value and
     * act on it.
     */
    private static class PlayerData {
        volatile boolean isLive       = false;
        volatile boolean persist      = false;
        volatile String  link         = "";
        volatile String  color        = "";
        @SerializedName("suffix") volatile String nametag = "";
        volatile boolean noSleep      = false;
        volatile String  twitchChannel = "";
        volatile String  lore          = "";
        volatile String  storedName    = "";
    }
}
