package com.fantac4t.fsp.config;

import com.fantac4t.fsp.FSP;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One-way conversion of the old JSON configs into the current {@link ModConfig}.
 *
 * <p>Two generations need handling: the original flat layout, where every option sat at the top
 * level, and the briefly-used sectioned JSON. Both feed into the same object, which is then written
 * out as TOML; the JSON file is renamed rather than deleted so a mistake is always recoverable.
 *
 * <p>This exists only for upgrades. Nothing here is used once a server has a config.toml.
 */
final class LegacyJsonConfig {
    private LegacyJsonConfig() {}

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final File JSON_FILE = new File(ModConfig.CONFIG_DIR, "config.json");

    static boolean exists() {
        return JSON_FILE.isFile();
    }

    /** Reads the JSON config in whichever layout it uses. Returns null if it can't be read. */
    static ModConfig convert() {
        try {
            String raw = Files.readString(JSON_FILE.toPath(), StandardCharsets.UTF_8);
            JsonObject onDisk = JsonParser.parseString(raw).getAsJsonObject();

            ModConfig converted = isFlatLayout(onDisk) ? fromFlat(onDisk) : fromSectioned(raw);
            FSP.LOGGER.info("Converting config.json ({} layout) to config.toml — your settings are "
                + "carried over, and every option now has a comment explaining it.",
                isFlatLayout(onDisk) ? "original flat" : "sectioned JSON");
            return converted;
        } catch (Exception e) {
            FSP.LOGGER.error("Could not read config.json to convert it — leaving it alone and starting "
                + "from defaults. Fix the JSON and delete config.toml to retry.", e);
            return null;
        }
    }

    /** Moves the JSON aside so it isn't converted again, but stays available if something looks wrong. */
    static void archive() {
        try {
            Path from = JSON_FILE.toPath();
            Path to = from.resolveSibling("config.json.converted-backup");
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
            FSP.LOGGER.info("Old config.json kept as {} — delete it once you're happy with config.toml.",
                to.getFileName());
        } catch (Exception e) {
            FSP.LOGGER.warn("Converted the config but couldn't rename config.json: {}", e.toString());
        }
    }

    /** A pre-sections config: the old flat keys sat at the top level, so none of the sections exist. */
    static boolean isFlatLayout(JsonObject onDisk) {
        boolean hasSection = onDisk.has("live") || onDisk.has("twitch") || onDisk.has("nosleep")
            || onDisk.has("voicechat") || onDisk.has("storage");
        if (hasSection) return false;
        return onDisk.has("livePlaceholder") || onDisk.has("twitchClientId") || onDisk.has("roles")
            || onDisk.has("noSleepPlaceholder") || onDisk.has("vcSpeakingIcon");
    }

    /** The short-lived sectioned-JSON layout maps straight onto the current object. */
    private static ModConfig fromSectioned(String raw) {
        ModConfig parsed = GSON.fromJson(raw, ModConfig.class);
        return parsed != null ? parsed : new ModConfig();
    }

    /**
     * Rebuilds a config from the original flat layout. Anything the old file didn't set keeps its
     * default. {@code vcMutedIcon} is intentionally dropped — Simple Voice Chat reports mute only to
     * the client, so that icon could never actually be shown.
     */
    static ModConfig fromFlat(JsonObject old) {
        ModConfig c = new ModConfig();

        c.live.placeholder        = str(old, "livePlaceholder",        c.live.placeholder);
        c.live.offlinePlaceholder = str(old, "notLivePlaceholder",     c.live.offlinePlaceholder);
        c.live.onMessage          = str(old, "liveOnMessage",          c.live.onMessage);
        c.live.offMessage         = str(old, "liveOffMessage",         c.live.offMessage);
        c.live.broadcastMessage   = str(old, "liveBroadcastMessage",   c.live.broadcastMessage);
        c.live.persistOnMessage   = str(old, "livePersistOnMessage",   c.live.persistOnMessage);
        c.live.persistOffMessage  = str(old, "livePersistOffMessage",  c.live.persistOffMessage);
        c.live.linkSetMessage     = str(old, "liveLinkSetMessage",     c.live.linkSetMessage);
        c.live.twitchNotConfiguredMessage =
            str(old, "liveLinkTwitchNotConfiguredMessage", c.live.twitchNotConfiguredMessage);

        c.twitch.clientId            = str(old, "twitchClientId",            c.twitch.clientId);
        c.twitch.clientSecret        = str(old, "twitchClientSecret",        c.twitch.clientSecret);
        c.twitch.pollIntervalSeconds = num(old, "twitchPollIntervalSeconds", c.twitch.pollIntervalSeconds);

        if (old.has("roles") && old.get("roles").isJsonObject()) {
            Map<String, String> groups = new LinkedHashMap<>();
            for (Map.Entry<String, JsonElement> e : old.getAsJsonObject("roles").entrySet()) {
                if (e.getValue().isJsonPrimitive()) groups.put(e.getKey(), e.getValue().getAsString());
            }
            // Assigned even when empty: an admin who cleared the map meant to turn role tags off,
            // and quietly handing back the defaults would undo that on upgrade.
            c.roles.groups = groups;
        }

        c.voicechat.speakingIcon     = str(old, "vcSpeakingIcon",     c.voicechat.speakingIcon);
        c.voicechat.deafenedIcon     = str(old, "vcDeafenedIcon",     c.voicechat.deafenedIcon);
        c.voicechat.disconnectedIcon = str(old, "vcDisconnectedIcon", c.voicechat.disconnectedIcon);
        c.voicechat.groupIcon        = str(old, "vcGroupIcon",        c.voicechat.groupIcon);

        c.nosleep.placeholder         = str(old, "noSleepPlaceholder",         c.nosleep.placeholder);
        c.nosleep.inactivePlaceholder = str(old, "noSleepNotPlaceholder",      c.nosleep.inactivePlaceholder);
        c.nosleep.onMessage           = str(old, "noSleepOnMessage",           c.nosleep.onMessage);
        c.nosleep.offMessage          = str(old, "noSleepOffMessage",          c.nosleep.offMessage);
        c.nosleep.broadcastOnMessage  = str(old, "noSleepBroadcastOnMessage",  c.nosleep.broadcastOnMessage);
        c.nosleep.broadcastOffMessage = str(old, "noSleepBroadcastOffMessage", c.nosleep.broadcastOffMessage);
        c.nosleep.bedTitle            = str(old, "noSleepBedTitle",            c.nosleep.bedTitle);
        c.nosleep.bedSubtitle         = str(old, "noSleepBedSubtitle",         c.nosleep.bedSubtitle);

        c.storage.autosaveIntervalMinutes =
            num(old, "autosaveIntervalMinutes", c.storage.autosaveIntervalMinutes);

        return c;
    }

    private static String str(JsonObject o, String key, String fallback) {
        return o.has(key) && o.get(key).isJsonPrimitive() ? o.get(key).getAsString() : fallback;
    }

    private static int num(JsonObject o, String key, int fallback) {
        try {
            return o.has(key) && o.get(key).isJsonPrimitive() ? o.get(key).getAsInt() : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
