package com.fantac4t.fsp.util;

import java.net.URI;

/**
 * Pure parsing helpers for player-supplied stream links.
 *
 * <p>Deliberately free of any Minecraft or Fabric types: this is the logic the Twitch poller depends
 * on to know which channel belongs to which player, so it needs to be testable on its own.
 */
public final class StreamLinks {
    private StreamLinks() {}

    /**
     * Pulls the channel name out of a Twitch URL, or returns null if the link isn't a Twitch one.
     *
     * <p>Accepts the shapes players actually paste — with or without a scheme or {@code www.}, with a
     * trailing slash, a sub-path, or tracking query parameters. The result is lower-cased because
     * Twitch logins are case-insensitive and the Helix API reports them in lower case.
     */
    public static String extractTwitchChannel(String url) {
        if (url == null || url.isBlank()) return null;

        URI uri;
        try {
            uri = URI.create(withScheme(url.trim()));
        } catch (IllegalArgumentException e) {
            return null; // not parseable as a URL at all
        }

        // Match on the host rather than searching the whole string, so a link that merely *contains*
        // "twitch.tv/" somewhere in its path (https://example.com/twitch.tv/someone) isn't mistaken
        // for a channel the player claimed.
        String host = uri.getHost();
        if (host == null) return null;
        host = host.toLowerCase();
        if (!host.equals("twitch.tv") && !host.endsWith(".twitch.tv")) return null;

        String path = uri.getPath();
        if (path == null || path.isBlank()) return null;

        // Query and fragment are already split off by URI; just take the first path segment.
        String channel = path.startsWith("/") ? path.substring(1) : path;
        int slash = channel.indexOf('/');
        if (slash >= 0) channel = channel.substring(0, slash);

        channel = channel.trim().toLowerCase();
        return channel.isBlank() ? null : channel;
    }

    /** Prefixes {@code https://} when a link was typed without a scheme. Blank in, blank out. */
    public static String withScheme(String s) {
        if (s == null || s.isBlank()) return "";
        if (!s.matches("(?i)https?://.*")) return "https://" + s;
        return s;
    }
}
