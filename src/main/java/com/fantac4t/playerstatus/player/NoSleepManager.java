package com.fantac4t.playerstatus.player;

import com.fantac4t.playerstatus.PlayerStatus;
import com.fantac4t.playerstatus.config.PlayerDataConfig;
import com.fantac4t.playerstatus.util.TextUtil;
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

public final class NoSleepManager {
    private NoSleepManager() {}

    public static String placeholder(UUID id) {
        if (id == null) return "";
        return PlayerDataConfig.isNoSleep(id)
                ? TextUtil.safe(PlayerStatus.CONFIG.noSleepPlaceholder)
                : TextUtil.safe(PlayerStatus.CONFIG.noSleepNotPlaceholder);
    }

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

    public static void registerBedEvent() {
        EntitySleepEvents.START_SLEEPING.register((entity, sleepingPos) -> {
            if (!(entity instanceof ServerPlayer sleeper)) return;

            MinecraftServer server = ((ServerLevel) sleeper.level()).getServer();
            List<String> noSleepNames = getOnlineNoSleepNames(server, sleeper);
            if (noSleepNames.isEmpty()) return;

            String joined = String.join(", ", noSleepNames);
            Component title    = TextUtil.parseMini(PlayerStatus.CONFIG.noSleepBedTitle.replace("{players}", joined));
            Component subtitle = TextUtil.parseMini(PlayerStatus.CONFIG.noSleepBedSubtitle.replace("{players}", joined));

            sleeper.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 20));
            sleeper.connection.send(new ClientboundSetTitleTextPacket(title));
            sleeper.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));

            sleeper.connection.send(new ClientboundSoundPacket(
                    Holder.direct(SoundEvents.VILLAGER_NO), SoundSource.MASTER,
                    sleeper.getX(), sleeper.getY(), sleeper.getZ(),
                    1.0f, 1.0f, sleeper.level().random.nextLong()
            ));
        });
    }

    private static List<String> getOnlineNoSleepNames(MinecraftServer server, ServerPlayer exclude) {
        List<UUID> noSleepIds = PlayerDataConfig.getNoSleepPlayers();
        List<String> names = new ArrayList<>();
        for (UUID id : noSleepIds) {
            if (id.equals(exclude.getUUID())) continue;
            ServerPlayer p = server.getPlayerList().getPlayer(id);
            if (p != null) names.add(p.getName().getString());
        }
        return names;
    }

    private static void broadcast(MinecraftServer server, ServerPlayer source, String template) {
        if (server == null || TextUtil.empty(template)) return;
        Component comp = buildMessage(template, source);
        server.getPlayerList().getPlayers().forEach(p -> p.sendSystemMessage(comp));
    }

    private static void sendTemplate(ServerPlayer player, String template) {
        if (TextUtil.empty(template)) return;
        player.sendSystemMessage(buildMessage(template, player));
    }

    private static Component buildMessage(String template, ServerPlayer player) {
        if (TextUtil.empty(template)) return Component.empty();
        return TextUtil.parseMini(template.replace("{player}", player.getName().getString()));
    }
}
