package com.fantac4t.fsp.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers config handling that rewrites a live server's file — getting it wrong silently discards an
 * admin's settings, which is the kind of damage you only notice much later.
 *
 * <p>Only pure logic is exercised; the file I/O around it is deliberately not involved.
 */
class ModConfigTest {

    private static JsonObject json(String raw) {
        return JsonParser.parseString(raw).getAsJsonObject();
    }

    /** Parses the TOML that save() would write, so assertions run against the real output. */
    private static CommentedConfig rendered(ModConfig c) {
        return TomlFormat.instance().createParser().parse(c.render());
    }

    @Nested
    @DisplayName("conversion from the original flat JSON")
    class FromFlatJson {

        private static final String FLAT = """
            {
              "livePlaceholder": "<gold>STREAMING</gold>",
              "notLivePlaceholder": "",
              "liveOnMessage": "<green>Live!</green>",
              "twitchClientId": "abc123",
              "twitchClientSecret": "shh",
              "twitchPollIntervalSeconds": 90,
              "roles": { "owner": "<gold>[Boss]</gold>", "custom": "<aqua>[Custom]</aqua>" },
              "vcSpeakingIcon": "S",
              "vcMutedIcon": "M",
              "vcDeafenedIcon": "D",
              "noSleepPlaceholder": "<red>X</red>",
              "noSleepBedTitle": "<red>No sleep</red>",
              "autosaveIntervalMinutes": 11
            }""";

        @Test
        @DisplayName("is detected for a flat file and not for a sectioned one")
        void detectsLayout() {
            assertTrue(LegacyJsonConfig.isFlatLayout(json(FLAT)));
            assertFalse(LegacyJsonConfig.isFlatLayout(json("{\"live\": {\"placeholder\": \"x\"}}")));
        }

        @Test
        @DisplayName("does not treat an empty file as flat")
        void doesNotMisdetectEmpty() {
            assertFalse(LegacyJsonConfig.isFlatLayout(json("{}")));
        }

        @Test
        @DisplayName("carries every customised value across to its new home")
        void carriesValuesAcross() {
            ModConfig c = LegacyJsonConfig.fromFlat(json(FLAT));

            assertEquals("<gold>STREAMING</gold>", c.live.placeholder);
            assertEquals("<green>Live!</green>",   c.live.onMessage);
            assertEquals("abc123",                 c.twitch.clientId);
            assertEquals("shh",                    c.twitch.clientSecret);
            assertEquals(90,                       c.twitch.pollIntervalSeconds);
            assertEquals("S",                      c.voicechat.speakingIcon);
            assertEquals("D",                      c.voicechat.deafenedIcon);
            assertEquals("<red>X</red>",           c.nosleep.placeholder);
            assertEquals("<red>No sleep</red>",    c.nosleep.bedTitle);
            assertEquals(11,                       c.storage.autosaveIntervalMinutes);
        }

        @Test
        @DisplayName("keeps custom role groups, including ones not in the defaults")
        void keepsRoles() {
            ModConfig c = LegacyJsonConfig.fromFlat(json(FLAT));

            assertEquals("<gold>[Boss]</gold>",   c.roles.groups.get("owner"));
            assertEquals("<aqua>[Custom]</aqua>", c.roles.groups.get("custom"));
            // The old file replaced the whole map, so defaults it didn't list are gone rather than
            // silently reappearing and re-tagging players the admin had deliberately untagged.
            assertFalse(c.roles.groups.containsKey("vip"));
        }

        @Test
        @DisplayName("respects an explicitly emptied roles map instead of restoring the defaults")
        void keepsDeliberatelyEmptyRoles() {
            ModConfig c = LegacyJsonConfig.fromFlat(json("{\"livePlaceholder\": \"x\", \"roles\": {}}"));
            assertTrue(c.roles.groups.isEmpty(),
                "expected no role groups, got " + c.roles.groups.keySet());
        }

        @Test
        @DisplayName("still uses the defaults when the old file had no roles key at all")
        void usesDefaultRolesWhenKeyAbsent() {
            ModConfig c = LegacyJsonConfig.fromFlat(json("{\"livePlaceholder\": \"x\"}"));
            assertEquals(new ModConfig().roles.groups, c.roles.groups);
        }

        @Test
        @DisplayName("leaves anything the old file didn't set at its default")
        void fillsGapsWithDefaults() {
            ModConfig defaults = new ModConfig();
            ModConfig c = LegacyJsonConfig.fromFlat(json(FLAT));

            assertEquals(defaults.live.offMessage,            c.live.offMessage);
            assertEquals(defaults.nosleep.broadcastOnMessage, c.nosleep.broadcastOnMessage);
            assertEquals(defaults.voicechat.groupIcon,        c.voicechat.groupIcon);
        }

        @Test
        @DisplayName("ignores a non-numeric poll interval rather than failing the whole conversion")
        void toleratesWrongTypes() {
            ModConfig defaults = new ModConfig();
            ModConfig c = LegacyJsonConfig.fromFlat(
                json("{\"livePlaceholder\": \"x\", \"twitchPollIntervalSeconds\": \"sixty\"}"));

            assertEquals(defaults.twitch.pollIntervalSeconds, c.twitch.pollIntervalSeconds);
        }

        @Test
        @DisplayName("drops vcMutedIcon, which the server can never observe")
        void dropsMutedIcon() {
            assertFalse(LegacyJsonConfig.fromFlat(json(FLAT)).render().contains("utedIcon"),
                "converted config should not carry a muted icon");
        }
    }

    @Nested
    @DisplayName("TOML output")
    class Toml {

        @Test
        @DisplayName("round-trips every option in the schema")
        void roundTripsEveryOption() {
            // Guards the schema table: a getter/setter pointing at the wrong field would survive a
            // compile but lose the value here.
            ModConfig original = LegacyJsonConfig.fromFlat(json("""
                {
                  "livePlaceholder": "P", "notLivePlaceholder": "NP", "liveOnMessage": "ON",
                  "liveOffMessage": "OFF", "liveBroadcastMessage": "BC", "livePersistOnMessage": "PON",
                  "livePersistOffMessage": "POFF", "liveLinkSetMessage": "LS",
                  "liveLinkTwitchNotConfiguredMessage": "TNC",
                  "twitchClientId": "ID", "twitchClientSecret": "SEC", "twitchPollIntervalSeconds": 45,
                  "vcSpeakingIcon": "SP", "vcDeafenedIcon": "DF", "vcDisconnectedIcon": "DC",
                  "vcGroupIcon": "GR",
                  "noSleepPlaceholder": "NSP", "noSleepNotPlaceholder": "NSN",
                  "noSleepOnMessage": "NSON", "noSleepOffMessage": "NSOFF",
                  "noSleepBroadcastOnMessage": "NSBON", "noSleepBroadcastOffMessage": "NSBOFF",
                  "noSleepBedTitle": "BT", "noSleepBedSubtitle": "BS",
                  "autosaveIntervalMinutes": 7
                }"""));

            CommentedConfig parsed = rendered(original);
            for (ModConfig.Opt opt : ModConfig.SCHEMA) {
                Object written = parsed.get(opt.path());
                assertNotNull(written, opt.path() + " missing from the written TOML");
                assertEquals(String.valueOf(opt.get().apply(original)), String.valueOf(written),
                    opt.path() + " did not survive the round trip");
            }
        }

        @Test
        @DisplayName("writes MiniMessage tags and quotes verbatim")
        void preservesAwkwardValues() {
            // These values carry <, >, ' and " — the characters most likely to break a naive writer.
            // Note the explicit String locals: Config.get() is generic, and letting it infer against
            // an overload like String.valueOf(char[]) picks char[] and blows up at runtime.
            ModConfig c = new ModConfig();
            CommentedConfig parsed = rendered(c);

            String broadcast = parsed.get("live.broadcastMessage");
            String subtitle  = parsed.get("nosleep.bedSubtitle");

            assertEquals(c.live.broadcastMessage, broadcast);
            assertTrue(broadcast.contains("<click:open_url:'{link}'>"),
                "the click tag's quoting should survive: " + broadcast);
            assertEquals(c.nosleep.bedSubtitle, subtitle);
        }

        @Test
        @DisplayName("keeps the non-ASCII icons intact")
        void preservesUnicodeIcons() {
            CommentedConfig parsed = rendered(new ModConfig());

            String speaking = parsed.get("voicechat.speakingIcon");
            String nosleep  = parsed.get("nosleep.placeholder");

            assertEquals("⌬", speaking);
            assertEquals("<red>☠</red>", nosleep);
        }

        @Test
        @DisplayName("comments every section, and they survive being written")
        void writesSectionComments() {
            String toml = new ModConfig().render();

            for (String section : ModConfig.SECTION_COMMENTS.keySet()) {
                assertTrue(toml.contains("[" + section + "]"), "missing section [" + section + "]");
            }
            assertTrue(toml.contains("# Flips a player's live status automatically"),
                "twitch section comment missing:\n" + toml);
            assertTrue(toml.contains("dev.twitch.tv/console/apps"),
                "the comment should tell an admin where to get credentials");
        }

        @Test
        @DisplayName("writes the roles table, and an emptied one stays empty")
        void writesRoles() {
            String owner = rendered(new ModConfig()).get("roles.groups.owner");
            assertEquals("<gold>[Owner]</gold>", owner);

            ModConfig cleared = new ModConfig();
            cleared.roles.groups.clear();
            CommentedConfig parsed = rendered(cleared);
            CommentedConfig groups = parsed.get("roles.groups");
            assertTrue(groups == null || groups.isEmpty(), "cleared roles should not come back");
        }

        @Test
        @DisplayName("has no duplicate paths in the schema")
        void schemaPathsAreUnique() {
            Set<String> seen = new HashSet<>();
            for (ModConfig.Opt opt : ModConfig.SCHEMA) {
                assertTrue(seen.add(opt.path()), "duplicate schema path: " + opt.path());
            }
        }
    }
}
