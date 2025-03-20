package me.beanes.acid.plugin.check.model;

import com.github.retrooper.packetevents.event.PacketSendEvent;

public interface SendPacketCheck {
    void onPacketSend(PacketSendEvent event);
}
