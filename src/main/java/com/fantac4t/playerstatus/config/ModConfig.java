package com.fantac4t.playerstatus.config;

import com.fantac4t.playerstatus.PlayerStatus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public final class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_DIR = new File("config/playerstatus");
    private static final File CONFIG_FILE = new File(CONFIG_DIR, "config.json");

    public String livePlaceholder = "<red><bold>LIVE</bold></red>";
    public String notLivePlaceholder = "";
    public String liveOnMessage = "<green>You are now live!</green>";
    public String liveOffMessage = "<yellow>You are no longer live.</yellow>";
    public String liveBroadcastMessage = "<gold>{player}</gold> is now live: <aqua><underline><click:open_url:{link}>{link}</click></underline></aqua>";
    public String livePersistOnMessage  = "<green>Auto live on reconnect: <bold>ENABLED</bold></green>";
    public String livePersistOffMessage = "<yellow>Auto live on reconnect: <bold>DISABLED</bold></yellow>";
    public String liveLinkSetMessage    = "<green>Stream link set to: <white>{link}</white></green>";

    public Map<String, String> roles = new HashMap<>();

    public String vcSpeakingIcon = "⌬";
    public String vcMutedIcon = "⌭";
    public String vcDeafenedIcon = "⌮";
    public String vcDisconnectedIcon = "⌯";
    public String vcGroupIcon = "⌰";

    public String noSleepPlaceholder = "<red>☠</red>";
    public String noSleepNotPlaceholder = "";
    public String noSleepOnMessage = "<red>You have toggled no-sleep on. Others will be warned when they try to sleep.</red>";
    public String noSleepOffMessage = "<green>You have toggled no-sleep off. Others can sleep peacefully.</green>";
    public String noSleepBroadcastOnMessage = "<red>{player} doesn't want to skip the night!</red>";
    public String noSleepBroadcastOffMessage = "<green>{player} is now okay with skipping the night.</green>";
    public String noSleepBedTitle = "<red>Can't skip the night!</red>";
    public String noSleepBedSubtitle = "<yellow>{players} doesn't want to sleep!</yellow>";

    public ModConfig() {
        roles.put("owner",     "<gold>[Owner]</gold>");
        roles.put("admin",     "<red>[Admin]</red>");
        roles.put("moderator", "<blue>[Mod]</blue>");
        roles.put("vip",       "<yellow>[VIP]</yellow>");
        roles.put("default",   "");
    }

    public static ModConfig load() {
        CONFIG_DIR.mkdirs();

        if (CONFIG_FILE.exists()) {
            try (Reader reader = new FileReader(CONFIG_FILE)) {
                ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
                if (loaded != null) return loaded;
            } catch (Exception e) {
                PlayerStatus.LOGGER.error("Failed to load config", e);
            }
        }

        ModConfig defaults = new ModConfig();
        defaults.save();
        return defaults;
    }

    public void save() {
        CONFIG_DIR.mkdirs();
        try (Writer writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (Exception e) {
            PlayerStatus.LOGGER.error("Failed to save config", e);
        }
    }
}
