package com.fantac4t.playerstatus.player;

import com.fantac4t.playerstatus.PlayerStatus;
import com.fantac4t.playerstatus.config.PlayerDataConfig;
import eu.pb4.placeholders.api.TextParserUtils;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class NoSleepManager {
    private NoSleepManager() {}

    /**
     * Returns the placeholder text — icon when active, empty when inactive.
     */
    public static String placeholder(UUID id) {
        if (id == null) return "";
        return PlayerDataConfig.isNoSleep(id)
                ? safe(PlayerStatus.CONFIG.noSleepPlaceholder)
                : safe(PlayerStatus.CONFIG.noSleepNotPlaceholder);
    }

    /**
     * Toggle no-sleep for a player. Sends personal + broadcast messages.
     */
    public static boolean toggle(ServerPlayer player) {
        UUID id = player.getUUID();
        boolean newState = !PlayerDataConfig.isNoSleep(id);
        PlayerDataConfig.setNoSleep(id, newState);

        if (newState) {
            sendTemplate(player, PlayerStatus.CONFIG.noSleepOnMessage);
            broadcast(((ServerLevel) player.level()).getServer(), player, PlayerStatus.CONFIG.noSleepBroadcastOnMessage);
        } else {
            sendTemplate(player, PlayerStatus.CONFIG.noSleepOffMessage);
            broadcast(((ServerLevel) player.level()).getServer(), player, PlayerStatus.CONFIG.noSleepBroadcastOffMessage);
        }
        return newState;
    }

    /**
     * Register the bed-enter event listener.
     * When a player gets into bed, check for online no-sleep players and warn the sleeper.
     */
    public static void registerBedEvent() {
        EntitySleepEvents.START_SLEEPING.register((entity, sleepingPos) -> {
            if (!(entity instanceof ServerPlayer sleeper)) return;

            MinecraftServer server = ((ServerLevel) sleeper.level()).getServer();
            List<String> noSleepNames = getOnlineNoSleepNames(server, sleeper);

            if (noSleepNames.isEmpty()) return;

            String joined = String.join(", ", noSleepNames);

            // Build title and subtitle from config templates
            String titleTemplate = PlayerStatus.CONFIG.noSleepBedTitle;
            String subtitleTemplate = PlayerStatus.CONFIG.noSleepBedSubtitle;

            Component title = parseMini(titleTemplate.replace("{players}", joined));
            Component subtitle = parseMini(subtitleTemplate.replace("{players}", joined));

            // Send title packets
            sleeper.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 20));
            sleeper.connection.send(new ClientboundSetTitleTextPacket(title));
            sleeper.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));

            // Play warning sound (only to the sleeper)
            sleeper.connection.send(new ClientboundSoundPacket(
                    Holder.direct(SoundEvents.VILLAGER_NO), SoundSource.MASTER,
                    sleeper.getX(), sleeper.getY(), sleeper.getZ(),
                    1.0f, 1.0f, sleeper.level().random.nextLong()
            ));
        });
    }

    /**
     * Get display names of online players who have no-sleep enabled (excluding the sleeper).
     */
    private static List<String> getOnlineNoSleepNames(MinecraftServer server, ServerPlayer exclude) {
        List<UUID> noSleepIds = PlayerDataConfig.getNoSleepPlayers();
        List<String> names = new ArrayList<>();

        for (UUID id : noSleepIds) {
            if (id.equals(exclude.getUUID())) continue;
            ServerPlayer p = server.getPlayerList().getPlayer(id);
            if (p != null) { // only online players
                names.add(p.getName().getString());
            }
        }
        return names;
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
        String expanded = template.replace("{player}", player.getName().getString());
        return parseMini(expanded);
    }

    private static Component parseMini(String text) {
        if (empty(text)) return Component.empty();
        try {
            return TextParserUtils.formatText(text);
        } catch (Throwable t) {
            return Component.literal(text);
        }
    }

    private static boolean empty(String s) { return s == null || s.isEmpty(); }
    private static String safe(String s) { return s == null ? "" : s; }
}
