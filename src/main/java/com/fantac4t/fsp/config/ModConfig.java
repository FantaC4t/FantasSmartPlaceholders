package com.fantac4t.fsp.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.fantac4t.fsp.FSP;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * The mod's config, stored as TOML so every option can carry a real comment explaining it.
 *
 * <p>Options are declared once in {@link #SCHEMA}: path, how to read it off this object, how to
 * write it back, and the comment to emit. Save and load both walk that one table, so a path can't
 * drift between the two directions.
 *
 * <p>Servers upgrading from a JSON config are converted automatically — see {@link LegacyJsonConfig}.
 */
public final class ModConfig {

    static final File CONFIG_DIR  = new File("config/Fanta's Placeholders");
    static final File CONFIG_FILE = new File(CONFIG_DIR, "config.toml");

    public Live      live      = new Live();
    public Twitch    twitch    = new Twitch();
    public Roles     roles     = new Roles();
    public VoiceChat voicechat = new VoiceChat();
    public NoSleep   nosleep   = new NoSleep();
    public Storage   storage   = new Storage();

    // ── Sections ────────────────────────────────────────────────────

    public static final class Live {
        public String placeholder        = "<red><bold>LIVE</bold></red>";
        public String offlinePlaceholder = "";
        public String onMessage          = "<green>You are now live!</green>";
        public String offMessage         = "<yellow>You are no longer live.</yellow>";
        public String broadcastMessage   = "<gold>{player}</gold> is now live: <aqua><underline><click:open_url:'{link}'>{link}</click></underline></aqua>";
        public String persistOnMessage   = "<green>Auto live on reconnect: <bold>ENABLED</bold></green>";
        public String persistOffMessage  = "<yellow>Auto live on reconnect: <bold>DISABLED</bold></yellow>";
        public String linkSetMessage     = "<green>Stream link set to: <white>{link}</white></green>";
        public String twitchNotConfiguredMessage =
            "<yellow>Note: this server hasn't set up Twitch auto-detection, so your live status won't update automatically. Use <white>/live</white> to toggle it yourself.</yellow>";
        public String linkNotTwitchMessage =
            "<yellow>Note: that doesn't look like a twitch.tv link, so it won't be auto-detected when you go live. Use a URL like <white>https://twitch.tv/yourname</white>, or toggle <white>/live</white> yourself.</yellow>";
    }

    public static final class Twitch {
        public String clientId            = "";
        public String clientSecret        = "";
        public int    pollIntervalSeconds = 60;
    }

    public static final class Roles {
        public Map<String, String> groups = new LinkedHashMap<>();

        public Roles() {
            groups.put("owner",     "<gold>[Owner]</gold>");
            groups.put("admin",     "<red>[Admin]</red>");
            groups.put("moderator", "<blue>[Mod]</blue>");
            groups.put("vip",       "<yellow>[VIP]</yellow>");
            groups.put("default",   "");
        }
    }

    public static final class VoiceChat {
        public String deafenedIcon     = "⌮";
        public String disconnectedIcon = "⌯";
        public String speakingIcon     = "⌬";
        public String groupIcon        = "⌰";
    }

    public static final class NoSleep {
        public String placeholder         = "<red>☠</red>";
        public String inactivePlaceholder = "";
        public String onMessage           = "<red>You have toggled no-sleep on. Others will be warned when they try to sleep.</red>";
        public String offMessage          = "<green>You have toggled no-sleep off. Others can sleep peacefully.</green>";
        public String broadcastOnMessage  = "<red>{player} doesn't want to skip the night!</red>";
        public String broadcastOffMessage = "<green>{player} is now okay with skipping the night.</green>";
        public String bedTitle            = "<red>Can't skip the night!</red>";
        public String bedSubtitle         = "<yellow>{players} doesn't want to sleep!</yellow>";
    }

    public static final class Storage {
        public int autosaveIntervalMinutes = 5;
    }

    // ── Schema ──────────────────────────────────────────────────────

    /** One configurable option: where it lives in the file, how to read/write it, and its comment. */
    record Opt(String path, Function<ModConfig, Object> get, BiConsumer<ModConfig, Object> set, String comment) {}

    /** Comments attached to the section headers themselves. */
    static final Map<String, String> SECTION_COMMENTS = new LinkedHashMap<>();
    static {
        SECTION_COMMENTS.put("live",
            "Live status, shown by %fsp:live%.\n"
          + "{player} and {link} are substituted. Values accept MiniMessage tags:\n"
          + "https://placeholders.pb4.eu/user/text-format/\n"
          + "Set any message to \"\" to silence it.");
        SECTION_COMMENTS.put("twitch",
            "Flips a player's live status automatically when they go live on Twitch.\n"
          + "Create an app at https://dev.twitch.tv/console/apps for a client ID and secret.\n"
          + "Leave either blank to disable auto-detection entirely.\n"
          + "Players opt in by running: /live link <their twitch.tv URL>\n"
          + "Changes take effect on /fsp reload - no restart needed.");
        SECTION_COMMENTS.put("roles",
            "Maps a LuckPerms primary group to the symbol shown by %fsp:role%.");
        SECTION_COMMENTS.put("voicechat",
            "Icons for %fsp:vc_status%. Requires Simple Voice Chat.\n"
          + "Checked in order: deafened, disconnected, speaking, in-group - first match wins.\n"
          + "A player in none of those states renders as nothing.\n"
          + "Note: a muted microphone is not listed. Simple Voice Chat reports mute only to\n"
          + "the client, so a server-side mod cannot see it.");
        SECTION_COMMENTS.put("nosleep",
            "The /nosleep status, shown by %fsp:nosleep%.\n"
          + "{player} is substituted in messages; {players} is substituted in the bed\n"
          + "title/subtitle with the names of everyone currently blocking sleep.");
        SECTION_COMMENTS.put("storage",
            "How player data (live status, colors, lore, nametags) is persisted.");
    }

    static final List<Opt> SCHEMA = List.of(
        new Opt("live.placeholder",        c -> c.live.placeholder,        (c, v) -> c.live.placeholder = (String) v,        "Shown while the player is live."),
        new Opt("live.offlinePlaceholder", c -> c.live.offlinePlaceholder, (c, v) -> c.live.offlinePlaceholder = (String) v, "Shown while they are not. Usually left empty."),
        new Opt("live.onMessage",          c -> c.live.onMessage,          (c, v) -> c.live.onMessage = (String) v,          "Sent to the player when they go live."),
        new Opt("live.offMessage",         c -> c.live.offMessage,         (c, v) -> c.live.offMessage = (String) v,         "Sent to the player when they stop."),
        new Opt("live.broadcastMessage",   c -> c.live.broadcastMessage,   (c, v) -> c.live.broadcastMessage = (String) v,   "Announced to everyone when a player goes live."),
        new Opt("live.persistOnMessage",   c -> c.live.persistOnMessage,   (c, v) -> c.live.persistOnMessage = (String) v,   "Confirmation for /live persist."),
        new Opt("live.persistOffMessage",  c -> c.live.persistOffMessage,  (c, v) -> c.live.persistOffMessage = (String) v,  null),
        new Opt("live.linkSetMessage",     c -> c.live.linkSetMessage,     (c, v) -> c.live.linkSetMessage = (String) v,     "Confirmation for /live link <url>."),
        new Opt("live.twitchNotConfiguredMessage", c -> c.live.twitchNotConfiguredMessage, (c, v) -> c.live.twitchNotConfiguredMessage = (String) v,
            "Shown when a player links a Twitch URL but the server has no Twitch credentials set."),
        new Opt("live.linkNotTwitchMessage", c -> c.live.linkNotTwitchMessage, (c, v) -> c.live.linkNotTwitchMessage = (String) v,
            "Shown when Twitch auto-detection IS configured but the link a player gave isn't a\n"
          + "twitch.tv URL (e.g. a typo like twitch.com) - so their /live status will never\n"
          + "update on its own, and nothing else would have told them that."),

        new Opt("twitch.clientId",            c -> c.twitch.clientId,            (c, v) -> c.twitch.clientId = (String) v,     null),
        new Opt("twitch.clientSecret",        c -> c.twitch.clientSecret,        (c, v) -> c.twitch.clientSecret = (String) v, "Keep this out of public repos and screenshots."),
        new Opt("twitch.pollIntervalSeconds", c -> c.twitch.pollIntervalSeconds, (c, v) -> c.twitch.pollIntervalSeconds = toInt(v, 60),
            "How often to ask Twitch who is live. Minimum 30; values below that are raised to it."),

        new Opt("voicechat.deafenedIcon",     c -> c.voicechat.deafenedIcon,     (c, v) -> c.voicechat.deafenedIcon = (String) v,     null),
        new Opt("voicechat.disconnectedIcon", c -> c.voicechat.disconnectedIcon, (c, v) -> c.voicechat.disconnectedIcon = (String) v, null),
        new Opt("voicechat.speakingIcon",     c -> c.voicechat.speakingIcon,     (c, v) -> c.voicechat.speakingIcon = (String) v,     null),
        new Opt("voicechat.groupIcon",        c -> c.voicechat.groupIcon,        (c, v) -> c.voicechat.groupIcon = (String) v,        null),

        new Opt("nosleep.placeholder",         c -> c.nosleep.placeholder,         (c, v) -> c.nosleep.placeholder = (String) v,         "Shown while the player has no-sleep on."),
        new Opt("nosleep.inactivePlaceholder", c -> c.nosleep.inactivePlaceholder, (c, v) -> c.nosleep.inactivePlaceholder = (String) v, null),
        new Opt("nosleep.onMessage",           c -> c.nosleep.onMessage,           (c, v) -> c.nosleep.onMessage = (String) v,           null),
        new Opt("nosleep.offMessage",          c -> c.nosleep.offMessage,          (c, v) -> c.nosleep.offMessage = (String) v,          null),
        new Opt("nosleep.broadcastOnMessage",  c -> c.nosleep.broadcastOnMessage,  (c, v) -> c.nosleep.broadcastOnMessage = (String) v,  null),
        new Opt("nosleep.broadcastOffMessage", c -> c.nosleep.broadcastOffMessage, (c, v) -> c.nosleep.broadcastOffMessage = (String) v, null),
        new Opt("nosleep.bedTitle",            c -> c.nosleep.bedTitle,            (c, v) -> c.nosleep.bedTitle = (String) v,            "Title shown to someone trying to sleep while others object."),
        new Opt("nosleep.bedSubtitle",         c -> c.nosleep.bedSubtitle,         (c, v) -> c.nosleep.bedSubtitle = (String) v,         null),

        new Opt("storage.autosaveIntervalMinutes", c -> c.storage.autosaveIntervalMinutes, (c, v) -> c.storage.autosaveIntervalMinutes = toInt(v, 5),
            "Minutes between player-data saves. Data is also written on disconnect and on a\n"
          + "clean shutdown; this timer is what protects you from a crash. Set to 0 to disable.")
    );

    private static final String ROLES_PATH = "roles.groups";

    private static int toInt(Object v, int fallback) {
        if (v instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(v).trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    // ── Loading ─────────────────────────────────────────────────────

    public static ModConfig load() {
        CONFIG_DIR.mkdirs();

        // A JSON config from an older version takes priority once, then is converted and set aside.
        if (!CONFIG_FILE.exists() && LegacyJsonConfig.exists()) {
            ModConfig converted = LegacyJsonConfig.convert();
            if (converted != null) {
                converted.save();
                LegacyJsonConfig.archive();
                return converted;
            }
        }

        if (!CONFIG_FILE.exists()) {
            ModConfig defaults = new ModConfig();
            defaults.save();
            return defaults;
        }

        try (CommentedFileConfig file = CommentedFileConfig.builder(CONFIG_FILE).sync().build()) {
            file.load();

            ModConfig c = new ModConfig();
            List<String> missing = new ArrayList<>();

            for (Opt opt : SCHEMA) {
                Object value = file.get(opt.path());
                if (value == null) missing.add(opt.path());
                else opt.set().accept(c, value);
            }

            Object groups = file.get(ROLES_PATH);
            if (groups == null) {
                missing.add(ROLES_PATH);
            } else if (groups instanceof Config sub) {
                // Assigned even when empty: clearing the table is how an admin turns role tags off.
                Map<String, String> parsed = new LinkedHashMap<>();
                for (Map.Entry<String, Object> e : sub.valueMap().entrySet()) {
                    parsed.put(e.getKey(), String.valueOf(e.getValue()));
                }
                c.roles.groups = parsed;
            }

            // Rewrite when the file predates an option, so new settings become visible and the
            // comments stay in step with the version that's actually running.
            if (!missing.isEmpty()) {
                c.save();
                FSP.LOGGER.info("config.toml: added {} option(s) introduced by this version, set to "
                    + "their defaults: {}", missing.size(), missing);
            }
            return c;

        } catch (Exception e) {
            // Do NOT overwrite a file we couldn't read — that would destroy the admin's config.
            FSP.LOGGER.error("Failed to parse config.toml — keeping the file as-is and running on "
                + "defaults. Fix the TOML and run /fsp reload.", e);
            return new ModConfig();
        }
    }

    public void save() {
        CONFIG_DIR.mkdirs();
        try {
            // REPLACE_ATOMIC: temp file + swap, so a crash mid-write can't truncate a working config.
            writer().write(ordered(), CONFIG_FILE, WritingMode.REPLACE_ATOMIC, StandardCharsets.UTF_8);
        } catch (Exception e) {
            FSP.LOGGER.error("Failed to save config.toml", e);
        }
    }

    /**
     * A config backed by insertion-ordered maps, filled in schema order.
     *
     * <p>night-config's default backing map is unordered, which scrambles both the sections and the
     * options inside them — unreadable for a file whose whole purpose is being hand-edited.
     */
    private CommentedConfig ordered() {
        CommentedConfig cfg = TomlFormat.newConfig(LinkedHashMap::new);
        writeInto(cfg);
        return cfg;
    }

    private static TomlWriter writer() {
        TomlWriter w = TomlFormat.instance().createWriter();
        w.setIndent(""); // sections are already visually grouped; indenting every line just adds noise
        return w;
    }

    /** Fills a config with this object's values, section comments and per-option comments. */
    void writeInto(CommentedConfig file) {
        for (Opt opt : SCHEMA) {
            file.set(opt.path(), opt.get().apply(this));
            if (opt.comment() != null) file.setComment(opt.path(), " " + opt.comment().replace("\n", "\n "));
        }

        Config groups = file.createSubConfig();
        roles.groups.forEach(groups::set);
        file.set(ROLES_PATH, groups);

        for (Map.Entry<String, String> e : SECTION_COMMENTS.entrySet()) {
            file.setComment(e.getKey(), " " + e.getValue().replace("\n", "\n "));
        }
    }

    /** Renders exactly what {@link #save()} would write, without touching the filesystem. */
    String render() {
        return writer().writeToString(ordered());
    }
}
