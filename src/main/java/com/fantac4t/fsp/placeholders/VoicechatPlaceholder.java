package com.fantac4t.fsp.placeholders;

import com.fantac4t.fsp.FSP;
import com.fantac4t.fsp.voicechat.VoicechatStateManager;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public final class VoicechatPlaceholder {

    private static final Identifier VC_STATUS_ID =
            Identifier.fromNamespaceAndPath(FSP.PLACEHOLDER_NS, "vc_status");

    public static void register() {
        //? if mc26 {
        Placeholders.registerServer(VC_STATUS_ID, (ctx, arg) -> {
        //?} else {
        /*Placeholders.register(VC_STATUS_ID, (ctx, arg) -> {
        *///?}
            if (!ctx.hasPlayer()) return PlaceholderResult.value("");
            UUID uuid = ctx.player().getUUID();

            // Deafened/disconnected always take priority
            if (VoicechatStateManager.isDeafened(uuid))
                return PlaceholderResult.value(FSP.CONFIG.voicechat.deafenedIcon);
            if (VoicechatStateManager.isDisconnected(uuid))
                return PlaceholderResult.value(FSP.CONFIG.voicechat.disconnectedIcon);

            if (VoicechatStateManager.isSpeaking(uuid))
                return PlaceholderResult.value(FSP.CONFIG.voicechat.speakingIcon);

            if (VoicechatStateManager.isInGroup(uuid))
                return PlaceholderResult.value(FSP.CONFIG.voicechat.groupIcon);

            return PlaceholderResult.value("");
        });
    }

    private VoicechatPlaceholder() {}
}
