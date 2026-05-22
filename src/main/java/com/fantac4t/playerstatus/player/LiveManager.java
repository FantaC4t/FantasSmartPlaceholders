package com.fantac4t.playerstatus.player;

import com.fantac4t.playerstatus.PlayerStatus;
import com.fantac4t.playerstatus.config.PlayerDataConfig;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.parsers.TagParser;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LiveManager {
    private LiveManager() {}

    // URL regex pattern
    private static final Pattern URL_PATTERN = Pattern.compile(
        "https?://(?:[-\\w.])+(?::\\d+)?(?:/[\\w\\-./_%?&=+#]*)?",
        Pattern.CASE_INSENSITIVE
    );

    public static String placeholder(UUID id) {
        return PlayerDataConfig.isLive(id)
                ? safe(PlayerStatus.CONFIG.livePlaceholder)
                : safe(PlayerStatus.CONFIG.notLivePlaceholder);
    }

    public static boolean toggleLive(ServerPlayer player) {
        UUID id = player.getUUID();
        boolean newState = !PlayerDataConfig.isLive(id);
        PlayerDataConfig.setLive(id, newState);

        if (newState) {
            sendTemplate(player, PlayerStatus.CONFIG.liveOnMessage);
            broadcast(((ServerLevel) player.level()).getServer(), player, PlayerStatus.CONFIG.liveBroadcastMessage);
        } else {
            sendTemplate(player, PlayerStatus.CONFIG.liveOffMessage);
        }
        return newState;
    }

    public static boolean togglePersist(ServerPlayer player) {
        UUID id = player.getUUID();
        boolean v = !PlayerDataConfig.persist(id);
        PlayerDataConfig.setPersist(id, v);
        player.sendSystemMessage(
                Component.literal("Auto live on reconnect: " + (v ? "ENABLED" : "DISABLED"))
                        .withStyle(v ? ChatFormatting.GREEN : ChatFormatting.YELLOW)
        );
        return v;
    }

    public static void setLink(ServerPlayer player, String link) {
        PlayerDataConfig.setLink(player.getUUID(), link);
        player.sendSystemMessage(Component.literal("Stream link set."));
    }

    private static void broadcast(MinecraftServer server, ServerPlayer source, String template) {
        if (server == null || empty(template)) return;

        Component comp = buildMessage(template, source);
        server.getPlayerList().getPlayers().forEach(p -> p.sendSystemMessage(comp));
    }

    private static void sendTemplate(ServerPlayer player, String template) {
        if (empty(template)) return;
        player.sendSystemMessage(buildMessage(template, player));
    }

    private static Component buildMessage(String template, ServerPlayer player) {
        if (empty(template)) return Component.empty();

        try {
            String expanded = expandCustomPlaceholders(template, player);
            String tagged   = ensureClickableUrlTags(expanded);

            Component withTags = TagParser.DEFAULT.parseText(tagged != null ? tagged : "", null);

            // 4) Resolve Placeholder API placeholders on the parsed component
            PlaceholderContext context = PlaceholderContext.of(player);
            Component finalComp = Placeholders.parseText(withTags, context);

            return finalComp;
        } catch (Throwable t) {
            PlayerStatus.LOGGER.warn("[PlayerStatus] Message building failed: {}", t.toString());
            return Component.literal(template);
        }
    }

    // Turn bare URLs into <click:open_url:'...'><underlined><aqua>...</click>
    private static String ensureClickableUrlTags(String input) {
        if (empty(input)) return "";
        // If author already used <click:...> tags, don't double-wrap
        if (input.contains("<click:")) return input;

        StringBuilder sb = new StringBuilder();
        Matcher m = URL_PATTERN.matcher(input);
        int last = 0;

        while (m.find()) {
            int start = m.start();
            int end = m.end();
            String url = input.substring(start, end);

            // Append text before the URL
            if (start > last) sb.append(input, last, start);

            // Wrap the URL in pb4 text tags
            // Note: single quotes around value are required
            sb.append("<aqua><underlined><click:open_url:'")
              .append(url)
              .append("'>")
              .append(url)
              .append("</click></underlined></aqua>");

            last = end;
        }

        // Append any remaining text
        if (last < input.length()) sb.append(input, last, input.length());

        return sb.toString();
    }

    private static String expandCustomPlaceholders(String template, ServerPlayer player) {
        String result = template;
        String name   = player.getName().getString();
        String link   = normalizeUrl(safe(PlayerDataConfig.getLink(player.getUUID())));

        // Support both {token} and %token% styles
        result = result.replace("{player}",      name);
        result = result.replace("%player_name%", name);
        result = result.replace("{link}",        link);
        result = result.replace("%link%",        link);

        return result;
    }

    private static String normalizeUrl(String s) {
        if (s == null || s.isBlank()) return "";
        if (!s.matches("(?i)https?://.*")) return "https://" + s;
        return s;
    }

    private static boolean empty(String s) {
        return s == null || s.isEmpty();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}