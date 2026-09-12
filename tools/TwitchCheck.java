import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Standalone check for the Twitch auto-live setup. Makes the same two calls the mod's poller makes,
 * so you can tell "my credentials/channel are wrong" apart from "the mod isn't wiring it up".
 *
 * Needs nothing but a JDK (the one running your server is fine) - no Gradle, no Minecraft:
 *
 *   java TwitchCheck.java <clientId> <clientSecret> <channel> [more channels...]
 *
 * Or, to keep the secret out of your shell history:
 *
 *   export TWITCH_CLIENT_ID=...
 *   export TWITCH_CLIENT_SECRET=...
 *   java TwitchCheck.java - - <channel>
 *
 * Tip: pass a channel you know is live right now alongside your own. If the known-live one shows
 * LIVE and yours doesn't, the setup is fine and yours simply isn't streaming.
 */
public class TwitchCheck {

    static final HttpClient HTTP = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("usage: java TwitchCheck.java <clientId> <clientSecret> <channel> [more...]");
            System.out.println("       (pass - for either credential to read it from TWITCH_CLIENT_ID / TWITCH_CLIENT_SECRET)");
            System.exit(2);
        }

        String clientId     = resolve(args[0], "TWITCH_CLIENT_ID");
        String clientSecret = resolve(args[1], "TWITCH_CLIENT_SECRET");
        List<String> channels = new ArrayList<>();
        for (int i = 2; i < args.length; i++) channels.add(args[i].trim().toLowerCase());

        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            fail("Client ID or secret is empty. This is exactly the state that leaves the poller "
                + "silently disabled - the mod logs 'Twitch poller not started' and does nothing.");
        }

        System.out.println("Checking " + channels.size() + " channel(s) as client " + mask(clientId));
        System.out.println();

        // ── 1. App access token (client_credentials), same as TwitchManager.refreshTokenIfNeeded ──
        System.out.println("[1/3] Requesting an app access token...");
        String form = "client_id=" + enc(clientId)
            + "&client_secret=" + enc(clientSecret)
            + "&grant_type=client_credentials";

        HttpResponse<String> tokenResp = HTTP.send(HttpRequest.newBuilder()
            .uri(URI.create("https://id.twitch.tv/oauth2/token"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(form))
            .timeout(Duration.ofSeconds(10))
            .build(), HttpResponse.BodyHandlers.ofString());

        if (tokenResp.statusCode() != 200) {
            fail("Token request returned HTTP " + tokenResp.statusCode() + ": " + tokenResp.body()
                + "\n      HTTP 400 'invalid client' means the client ID or secret is wrong, or the secret was rotated.");
        }
        String token = extract(tokenResp.body(), "access_token");
        if (token == null) fail("No access_token in the response: " + tokenResp.body());
        System.out.println("      OK - credentials are valid.");
        System.out.println();

        // ── 2. Do these channels exist? The mod can't tell a typo from "not streaming" ──
        System.out.println("[2/3] Checking the channel names exist...");
        StringBuilder usersUrl = new StringBuilder("https://api.twitch.tv/helix/users?");
        for (int i = 0; i < channels.size(); i++) {
            if (i > 0) usersUrl.append('&');
            usersUrl.append("login=").append(enc(channels.get(i)));
        }
        String usersBody = get(usersUrl.toString(), token, clientId);
        List<String> found = extractAll(usersBody, "login");
        for (String ch : channels) {
            System.out.println("      " + (found.contains(ch) ? "exists    " : "NOT FOUND ") + ch);
        }
        if (found.size() < channels.size()) {
            System.out.println("      ^ A name not found here will never go live in-game, no matter what the");
            System.out.println("        player streams - check the URL they gave to /live link.");
        }
        System.out.println();

        // ── 3. Who is live right now - the exact query the poller runs ──
        System.out.println("[3/3] Querying /helix/streams (the poller's actual call)...");
        StringBuilder streamsUrl = new StringBuilder("https://api.twitch.tv/helix/streams?first=100");
        for (String ch : channels) streamsUrl.append("&user_login=").append(enc(ch));

        String streamsBody = get(streamsUrl.toString(), token, clientId);
        List<String> live = extractAll(streamsBody, "user_login");

        System.out.println();
        for (String ch : channels) {
            System.out.println("      " + (live.contains(ch) ? "LIVE    " : "offline ") + ch);
        }
        System.out.println();
        System.out.println("The mod flips a player's in-game status to match the LIVE/offline column above,");
        System.out.println("within one poll interval (default 60s), for players who are online at the time.");
    }

    static String get(String url, String token, String clientId) throws Exception {
        HttpResponse<String> r = HTTP.send(HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + token)
            .header("Client-Id", clientId)
            .timeout(Duration.ofSeconds(10))
            .GET().build(), HttpResponse.BodyHandlers.ofString());

        if (r.statusCode() != 200) {
            fail("HTTP " + r.statusCode() + " from " + url + "\n      " + r.body()
                + "\n      A 401 here means the Client-Id and the token belong to different apps.");
        }
        return r.body();
    }

    /** Minimal string scraping so this file stays dependency-free - enough for these two responses. */
    static String extract(String json, String key) {
        List<String> all = extractAll(json, key);
        return all.isEmpty() ? null : all.get(0);
    }

    static List<String> extractAll(String json, String key) {
        List<String> out = new ArrayList<>();
        String needle = "\"" + key + "\"";
        int i = 0;
        while ((i = json.indexOf(needle, i)) >= 0) {
            int colon = json.indexOf(':', i + needle.length());
            if (colon < 0) break;
            int start = json.indexOf('"', colon + 1);
            if (start < 0) break;
            int end = json.indexOf('"', start + 1);
            if (end < 0) break;
            out.add(json.substring(start + 1, end).toLowerCase());
            i = end;
        }
        return out;
    }

    static String resolve(String arg, String envVar) {
        return "-".equals(arg) ? System.getenv(envVar) : arg;
    }

    static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    static String mask(String s) {
        return s.length() <= 6 ? "***" : s.substring(0, 4) + "..." + s.substring(s.length() - 2);
    }

    static void fail(String message) {
        System.out.println();
        System.out.println("FAILED: " + message);
        System.exit(1);
    }
}
