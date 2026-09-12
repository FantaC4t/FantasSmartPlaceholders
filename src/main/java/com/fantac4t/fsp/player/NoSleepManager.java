package com.fantac4t.fsp.player;

import com.fantac4t.fsp.FSP;
import com.fantac4t.fsp.config.PlayerDataConfig;
import com.fantac4t.fsp.util.Messages;
import com.fantac4t.fsp.util.TextUtil;
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
                ? TextUtil.safe(FSP.CONFIG.nosleep.placeholder)
                : TextUtil.safe(FSP.CONFIG.nosleep.inactivePlaceholder);
    }

    public static boolean toggle(ServerPlayer player) {
        UUID id = player.getUUID();
        boolean newState = !PlayerDataConfig.isNoSleep(id);
        PlayerDataConfig.setNoSleep(id, newState);

        if (newState) {
            Messages.send(player, FSP.CONFIG.nosleep.onMessage);
            Messages.broadcast(((ServerLevel) player.level()).getServer(), player, FSP.CONFIG.nosleep.broadcastOnMessage);
        } else {
            Messages.send(player, FSP.CONFIG.nosleep.offMessage);
            Messages.broadcast(((ServerLevel) player.level()).getServer(), player, FSP.CONFIG.nosleep.broadcastOffMessage);
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
            Component title    = TextUtil.parseMini(FSP.CONFIG.nosleep.bedTitle.replace("{players}", joined));
            Component subtitle = TextUtil.parseMini(FSP.CONFIG.nosleep.bedSubtitle.replace("{players}", joined));

            sleeper.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 20));
            sleeper.connection.send(new ClientboundSetTitleTextPacket(title));
            sleeper.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));

            sleeper.connection.send(new ClientboundSoundPacket(
                    Holder.direct(SoundEvents.VILLAGER_NO), SoundSource.MASTER,
                    sleeper.getX(), sleeper.getY(), sleeper.getZ(),
                    //? if mc26 {
                    1.0f, 1.0f, sleeper.getRandom().nextLong()
                    //?} else {
                    /*1.0f, 1.0f, sleeper.level().random.nextLong()
                    *///?}
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

}
