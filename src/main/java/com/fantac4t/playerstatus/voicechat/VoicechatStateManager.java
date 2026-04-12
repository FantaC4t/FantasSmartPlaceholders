package com.fantac4t.playerstatus.voicechat;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks per-player voice chat state on the server thread.
 * Updated by VoicechatPlugin via API events.
 */
public final class VoicechatStateManager {

    private VoicechatStateManager() {}

    /** Millis since last mic packet counts as "still speaking" */
    private static final long SPEAKING_TIMEOUT_MS = 600L;

    /** UUID -> timestamp (ms) of the last received microphone packet */
    private static final Map<UUID, Long> lastSpeakingTime = new ConcurrentHashMap<>();

    /** Players who have voice chat disabled (deafened themselves) */
    private static final Set<UUID> deafened = ConcurrentHashMap.newKeySet();

    /** Players who are disconnected from voice chat entirely */
    private static final Set<UUID> disconnected = ConcurrentHashMap.newKeySet();

    /** Players currently in a voice chat group */
    private static final Set<UUID> inGroup = ConcurrentHashMap.newKeySet();

    // --- Called by VoicechatPlugin ---

    public static void onMicPacket(UUID uuid) {
        lastSpeakingTime.put(uuid, System.currentTimeMillis());
    }

    public static void onStateChanged(UUID uuid, boolean isDisabled, boolean isDisconnected) {
        if (isDisabled) {
            deafened.add(uuid);
        } else {
            deafened.remove(uuid);
        }

        if (isDisconnected) {
            disconnected.add(uuid);
            // Can't speak if disconnected
            lastSpeakingTime.remove(uuid);
        } else {
            disconnected.remove(uuid);
        }
    }

    public static void onGroupJoin(UUID uuid) {
        inGroup.add(uuid);
    }

    public static void onGroupLeave(UUID uuid) {
        inGroup.remove(uuid);
    }

    public static void onDisconnect(UUID uuid) {
        disconnected.add(uuid);
        deafened.remove(uuid);
        lastSpeakingTime.remove(uuid);
        inGroup.remove(uuid);
    }

    // --- Queried by placeholders ---

    public static boolean isSpeaking(UUID uuid) {
        Long last = lastSpeakingTime.get(uuid);
        if (last == null) return false;
        return (System.currentTimeMillis() - last) < SPEAKING_TIMEOUT_MS;
    }

    public static boolean isDeafened(UUID uuid) {
        return deafened.contains(uuid);
    }

    public static boolean isDisconnected(UUID uuid) {
        return disconnected.contains(uuid);
    }

    public static boolean isInGroup(UUID uuid) {
        return inGroup.contains(uuid);
    }
}
