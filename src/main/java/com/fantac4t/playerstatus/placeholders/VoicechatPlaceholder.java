package com.fantac4t.playerstatus.placeholders;

import com.fantac4t.playerstatus.PlayerStatus;
import com.fantac4t.playerstatus.voicechat.VoicechatStateManager;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public final class VoicechatPlaceholder {

    private static final Identifier VC_STATUS_ID =
            Identifier.fromNamespaceAndPath(PlayerStatus.MOD_ID, "vc_status");

    public static void register() {
        Placeholders.register(VC_STATUS_ID, (ctx, arg) -> {
            if (!ctx.hasPlayer()) return PlaceholderResult.value("");
            UUID uuid = ctx.player().getUUID();

            // Deafened/disconnected always take priority
            if (VoicechatStateManager.isDeafened(uuid))
                return PlaceholderResult.value(PlayerStatus.CONFIG.vcDeafenedIcon);
            if (VoicechatStateManager.isDisconnected(uuid))
                return PlaceholderResult.value(PlayerStatus.CONFIG.vcDisconnectedIcon);

            if (VoicechatStateManager.isSpeaking(uuid))
                return PlaceholderResult.value(PlayerStatus.CONFIG.vcSpeakingIcon);

            if (VoicechatStateManager.isInGroup(uuid))
                return PlaceholderResult.value(PlayerStatus.CONFIG.vcGroupIcon);

            return PlaceholderResult.value("");
        });
    }

    private VoicechatPlaceholder() {}
}
