package com.fantac4t.playerstatus.config;

import com.fantac4t.playerstatus.PlayerStatus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public final class PlayerDataConfig {
    private static final Map<UUID, PlayerData> PLAYER_DATA = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File PLAYER_DATA_FILE;
    private static boolean dirty = false;

    public static void load() {
        File dir = new File("config/playerstatus");
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
                        UUID id = UUID.fromString(e.getKey());
                        PlayerData pd = e.getValue();
                        pd.id = id;
                        PLAYER_DATA.put(id, pd);
                    } catch (IllegalArgumentException ex) {
                        PlayerStatus.LOGGER.warn("Skipping invalid UUID key: " + e.getKey());
                    }
                }
            }
        } catch (Exception ex) {
            PlayerStatus.LOGGER.error("Failed to load player_data.json. NOT overwriting file. Fix JSON manually.", ex);
        }
    }

    private static void markDirty() { dirty = true; }

    public static void save() {
        if (!dirty) return;
        if (PLAYER_DATA_FILE == null) return;
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

    // Helper method to get or create player data
    private static PlayerData get(UUID id) {
        return PLAYER_DATA.computeIfAbsent(id, PlayerData::new);
    }

    // Live status methods
    public static boolean isLive(UUID id) {
        // Your existing implementation to check if a player is live
        boolean result = get(id).isLive;
        
        // Add this debug logging
        PlayerStatus.LOGGER.debug("Checking if player {} is live: {}", id, result);
        
        return result;
    }

    public static void setLive(UUID id, boolean value) {
        get(id).isLive = value;
        markDirty();
    }

    public static boolean persist(UUID id) {
        return get(id).persist;
    }

    public static void setPersist(UUID id, boolean value) {
        get(id).persist = value;
        markDirty();
    }

    public static String getLink(UUID playerId) {
        // Return the stored stream link for the player
        String link = get(playerId).link;
        PlayerStatus.LOGGER.debug("Getting stream link for player {}: {}", playerId, link);
        return link;
    }

    public static void setLink(UUID playerId, String link) {
        // Store the stream link for the player
        get(playerId).link = link;
        markDirty();
    }

    // Color methods
    public static String getColor(UUID id) {
        return get(id).color;
    }

    public static void setColor(UUID id, String value) {
        get(id).color = value;
        markDirty();
    }

    public static void clearColor(UUID id) {
        PlayerData pd = get(id);
        pd.color = "";
        markDirty();
    }
    
    // Simple suffix (role) methods
    public static String getSuffix(UUID id) {
        return get(id).role; // role field reused as suffix
    }

    public static void setSuffix(UUID id, String value) {
        get(id).role = value;
        markDirty();
    }

    public static void clearSuffix(UUID id) {
        get(id).role = "";
        markDirty();
    }

    // PlayerData class representing all data for a single player
    private static class PlayerData {
        private UUID id;
        private boolean isLive = false;
        private boolean persist = false;
        private String link = "";
        private String color = "";
        private String role = ""; // suffix text
        private PlayerData(UUID id) { this.id = id; }
    }
}