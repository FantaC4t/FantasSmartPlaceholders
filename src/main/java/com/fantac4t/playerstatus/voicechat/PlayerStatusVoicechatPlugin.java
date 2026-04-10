package com.fantac4t.playerstatus.voicechat;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.PlayerDisconnectedEvent;
import de.maxhenkel.voicechat.api.events.PlayerStateChangedEvent;

public class PlayerStatusVoicechatPlugin implements VoicechatPlugin {

    @Override
    public String getPluginId() {
        return "playerstatus";
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
        registration.registerEvent(PlayerStateChangedEvent.class, this::onPlayerStateChanged);
        registration.registerEvent(PlayerDisconnectedEvent.class, this::onPlayerDisconnected);
    }

    private void onMicrophonePacket(MicrophonePacketEvent event) {
        VoicechatConnection sender = event.getSenderConnection();
        if (sender != null) {
            VoicechatStateManager.onMicPacket(sender.getPlayer().getUuid());
        }
    }

    private void onPlayerStateChanged(PlayerStateChangedEvent event) {
        VoicechatStateManager.onStateChanged(
                event.getPlayerUuid(),
                event.isDisabled(),
                event.isDisconnected()
        );
    }

    private void onPlayerDisconnected(PlayerDisconnectedEvent event) {
        VoicechatStateManager.onDisconnect(event.getPlayerUuid());
    }
}
