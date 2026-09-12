package com.fantac4t.fsp.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * These cover the link parsing the Twitch poller depends on: if this returns the wrong channel, or
 * null when it shouldn't, a player's live status silently never updates — which is exactly the
 * failure that is hardest to notice on a live server.
 */
class StreamLinksTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @DisplayName("extracts the channel from the URL shapes players actually paste")
    @CsvSource({
        "https://twitch.tv/fantac4t,                      fantac4t",
        "https://www.twitch.tv/fantac4t,                  fantac4t",
        "http://twitch.tv/fantac4t,                       fantac4t",
        "twitch.tv/fantac4t,                              fantac4t",
        "www.twitch.tv/fantac4t,                          fantac4t",
        "https://twitch.tv/fantac4t/,                     fantac4t",
        "https://twitch.tv/fantac4t/videos,               fantac4t",
        "https://twitch.tv/fantac4t?referrer=raid,        fantac4t",
        "https://twitch.tv/fantac4t#about,                fantac4t",
        "https://m.twitch.tv/fantac4t,                    fantac4t",
    })
    void extractsChannel(String url, String expected) {
        assertEquals(expected, StreamLinks.extractTwitchChannel(url));
    }

    @Test
    @DisplayName("lower-cases the channel, since Helix reports logins in lower case")
    void lowercasesChannel() {
        // The poller compares this against user_login from the Twitch API, which is always lower
        // case — a capitalised link would otherwise never match and the player would never go live.
        assertEquals("fantac4t", StreamLinks.extractTwitchChannel("https://Twitch.tv/FantaC4t"));
    }

    @ParameterizedTest
    @DisplayName("returns null for links that are not Twitch channels")
    @ValueSource(strings = {
        "https://youtube.com/@fantac4t",
        "https://kick.com/fantac4t",
        "https://example.com/twitch",
        "https://twitch.tv/",
        "https://twitch.tv",
        "not a url at all",
        "   ",
    })
    void returnsNullForNonTwitch(String url) {
        assertNull(StreamLinks.extractTwitchChannel(url));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("handles null and empty input without throwing")
    void handlesNullAndEmpty(String url) {
        assertNull(StreamLinks.extractTwitchChannel(url));
    }

    @ParameterizedTest
    @DisplayName("only matches twitch.tv as a host, not as part of a path or a lookalike domain")
    @ValueSource(strings = {
        "https://example.com/twitch.tv/fantac4t", // twitch.tv in the path, not the host
        "https://nottwitch.tv/fantac4t",          // lookalike domain
        "https://twitch.tv.evil.com/fantac4t",    // twitch.tv as a subdomain of something else
    })
    void doesNotMatchTwitchOutsideTheHost(String url) {
        // Treating these as Twitch links would point the poller at a channel the player never
        // claimed, and could flip their live status based on someone else's stream.
        assertNull(StreamLinks.extractTwitchChannel(url));
    }

    @Test
    @DisplayName("adds https:// only when a scheme is missing")
    void withScheme() {
        assertEquals("https://twitch.tv/x", StreamLinks.withScheme("twitch.tv/x"));
        assertEquals("https://twitch.tv/x", StreamLinks.withScheme("https://twitch.tv/x"));
        assertEquals("http://twitch.tv/x",  StreamLinks.withScheme("http://twitch.tv/x"));
        assertEquals("https://twitch.tv/x", StreamLinks.withScheme("HTTPS://twitch.tv/x").replace("HTTPS", "https"));
        assertEquals("", StreamLinks.withScheme(null));
        assertEquals("", StreamLinks.withScheme("   "));
    }
}
