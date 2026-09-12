package com.fantac4t.fsp.voicechat;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.*;

public class FSPVoicechatPlugin implements VoicechatPlugin {

    @Override
    public String getPluginId() {
        return "fanta-smart-placeholders";
    }

    @Override
    public void initialize(VoicechatApi api) {
        com.fantac4t.fsp.FSP.LOGGER.info("[FSP] Successfully hooked into Simple Voice Chat!");
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
        registration.registerEvent(PlayerStateChangedEvent.class, this::onPlayerStateChanged);
        registration.registerEvent(PlayerDisconnectedEvent.class, this::onPlayerDisconnected);
        registration.registerEvent(JoinGroupEvent.class, this::onJoinGroup);
        registration.registerEvent(LeaveGroupEvent.class, this::onLeaveGroup);
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

    private void onJoinGroup(JoinGroupEvent event) {
        VoicechatStateManager.onGroupJoin(
                event.getConnection().getPlayer().getUuid()
        );
    }

    private void onLeaveGroup(LeaveGroupEvent event) {
        VoicechatStateManager.onGroupLeave(
                event.getConnection().getPlayer().getUuid()
        );
    }
}
