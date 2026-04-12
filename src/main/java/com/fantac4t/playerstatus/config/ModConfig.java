package com.fantac4t.playerstatus.config;

import com.fantac4t.playerstatus.PlayerStatus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ModConfig {
    // Live settings with formatting codes
    public String livePlaceholder = "<red><bold>LIVE</bold></red>";
    public String notLivePlaceholder = "";
    public String liveOnMessage = "<green>You are now live!</green>";
    public String liveOffMessage = "<yellow>You are no longer live.</yellow>";
    // Use direct click formatting for links
    public String liveBroadcastMessage = "<gold>{player}</gold> is now live: <aqua><underline><click:open_url:{link}>{link}</click></underline></aqua>";
    
    // Role settings
    public Map<String, String> roles = new HashMap<>();
    
    // Voice chat status icon configurations (mapped in resource pack font)
    public String vcSpeakingIcon = "\u232C";      // ⌬ vc_talking
    public String vcMutedIcon = "\u232D";         // ⌭ vc_microphone_muted
    public String vcDeafenedIcon = "\u232E";      // ⌮ vc_disabled
    public String vcDisconnectedIcon = "\u232F";  // ⌯ vc_disconnected
    public String vcGroupIcon = "\u2330";         // ⌰ vc_group (texture TBD)

    // No-sleep settings
    public String noSleepPlaceholder = "<red>\u2620</red>";  // ☠ skull icon when active
    public String noSleepNotPlaceholder = "";                 // empty when inactive
    public String noSleepOnMessage = "<red>You have toggled no-sleep on. Others will be warned when they try to sleep.</red>";
    public String noSleepOffMessage = "<green>You have toggled no-sleep off. Others can sleep peacefully.</green>";
    public String noSleepBroadcastOnMessage = "<red>{player} doesn't want to skip the night!</red>";
    public String noSleepBroadcastOffMessage = "<green>{player} is now okay with skipping the night.</green>";
    public String noSleepBedTitle = "<red>Can't skip the night!</red>";
    public String noSleepBedSubtitle = "<yellow>{players} doesn't want to sleep!</yellow>";
    
    public ModConfig() {
        // Initialize with text-based role symbols
        roles.put("supporter", "$");
        roles.put("owner", "^");
        roles.put("member", "");
    }

    public static ModConfig load() {
        File configDir = new File("config/playerstatus");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        File file = new File(configDir, "config.json");
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                return new GsonBuilder().setPrettyPrinting().create().fromJson(reader, ModConfig.class);
            } catch (Exception e) {
                PlayerStatus.LOGGER.error("Failed to load config", e);
            }
        }

        // Save defaults if file doesn't exist
        ModConfig config = new ModConfig();
        config.save();
        return config;
    }

    public void save() {
        try {
            File configDir = new File("config/playerstatus");
            if (!configDir.exists()) {
                configDir.mkdirs();
            }
            
            File file = new File(configDir, "config.json");
            if (!file.exists()) {
                file.createNewFile();
            }

            try (Writer writer = new FileWriter(file)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(this, writer);
            }
        } catch (Exception e) {
            PlayerStatus.LOGGER.error("Failed to save config", e);
        }
    }
}