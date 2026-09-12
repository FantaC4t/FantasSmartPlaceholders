package com.fantac4t.fsp.util;

import com.fantac4t.fsp.FSP;
import com.fantac4t.fsp.config.PlayerDataConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds and sends the configurable, player-facing messages.
 *
 * <p>Shared by every feature that announces something, so the custom placeholders expand the same
 * way everywhere and a broken config template can never take down the command or event sending it.
 */
public final class Messages {
    private Messages() {}

    private static final Pattern URL_PATTERN = Pattern.compile(
        "https?://(?:[-\\w.])+(?::\\d+)?(?:/[\\w\\-./_%?&=+#]*)?",
        Pattern.CASE_INSENSITIVE
    );

    /** Sends a template to a single player. Blank templates are skipped, so a feature can be muted by emptying it. */
    public static void send(ServerPlayer player, String template) {
        if (TextUtil.empty(template)) return;
        player.sendSystemMessage(build(template, player));
    }

    /** Sends a template, expanded against {@code source}, to every online player. */
    public static void broadcast(MinecraftServer server, ServerPlayer source, String template) {
        if (server == null || TextUtil.empty(template)) return;
        Component comp = build(template, source);
        server.getPlayerList().getPlayers().forEach(p -> p.sendSystemMessage(comp));
    }

    /**
     * Expands the custom placeholders, auto-links bare URLs, and parses the result as MiniMessage.
     * Falls back to the raw template instead of throwing — one malformed config line should not
     * break the action that triggered the message.
     */
    public static Component build(String template, ServerPlayer player) {
        if (TextUtil.empty(template)) return Component.empty();
        try {
            return TextUtil.parseMini(autoLinkUrls(expandPlaceholders(template, player)), player);
        } catch (Throwable t) {
            FSP.LOGGER.warn("[FSP] Message building failed: {}", t.toString());
            return Component.literal(template);
        }
    }

    private static String expandPlaceholders(String template, ServerPlayer player) {
        String name = player.getName().getString();
        // Player-controlled, so it must not be able to open a MiniMessage tag of its own.
        String link = TextUtil.escapeMini(StreamLinks.withScheme(TextUtil.safe(PlayerDataConfig.getLink(player.getUUID()))));
        return template
                .replace("{player}",      name)
                .replace("%player_name%", name)
                .replace("{link}",        link)
                .replace("%link%",        link);
    }

    /** Wraps bare URLs in click/hover tags, unless the template already does its own linking. */
    private static String autoLinkUrls(String input) {
        if (TextUtil.empty(input)) return "";
        if (input.contains("<click:")) return input;

        StringBuilder sb = new StringBuilder();
        Matcher m = URL_PATTERN.matcher(input);
        int last = 0;

        while (m.find()) {
            if (m.start() > last) sb.append(input, last, m.start());
            String url = input.substring(m.start(), m.end());
            sb.append("<hover:show_text:'<gray>Click to open: ").append(url).append("</gray>'>")
              .append("<click:open_url:'").append(url).append("'>")
              .append("<aqua><underlined>").append(url).append("</underlined></aqua>")
              .append("</click></hover>");
            last = m.end();
        }

        if (last < input.length()) sb.append(input, last, input.length());
        return sb.toString();
    }
}
