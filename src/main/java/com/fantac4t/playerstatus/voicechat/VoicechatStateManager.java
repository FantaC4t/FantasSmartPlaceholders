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

    /** Players who have voice chat disabled (muted themselves) */
    private static final Set<UUID> muted = ConcurrentHashMap.newKeySet();

    /** Players who are disconnected from voice chat entirely */
    private static final Set<UUID> disconnected = ConcurrentHashMap.newKeySet();

    // --- Called by VoicechatPlugin ---

    public static void onMicPacket(UUID uuid) {
        lastSpeakingTime.put(uuid, System.currentTimeMillis());
    }

    public static void onStateChanged(UUID uuid, boolean isDisabled, boolean isDisconnected) {
        if (isDisabled) {
            muted.add(uuid);
        } else {
            muted.remove(uuid);
        }

        if (isDisconnected) {
            disconnected.add(uuid);
            // Can't speak if disconnected
            lastSpeakingTime.remove(uuid);
        } else {
            disconnected.remove(uuid);
        }
    }

    public static void onDisconnect(UUID uuid) {
        disconnected.add(uuid);
        muted.remove(uuid);
        lastSpeakingTime.remove(uuid);
    }

    // --- Queried by placeholders ---

    public static boolean isSpeaking(UUID uuid) {
        Long last = lastSpeakingTime.get(uuid);
        if (last == null) return false;
        return (System.currentTimeMillis() - last) < SPEAKING_TIMEOUT_MS;
    }

    public static boolean isMuted(UUID uuid) {
        return muted.contains(uuid);
    }

    public static boolean isDisconnected(UUID uuid) {
        return disconnected.contains(uuid);
    }
}
