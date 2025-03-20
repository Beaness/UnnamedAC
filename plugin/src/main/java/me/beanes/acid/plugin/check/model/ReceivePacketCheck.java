package me.beanes.acid.plugin.check.model;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;

public interface ReceivePacketCheck {
    void onPacketReceive(PacketReceiveEvent event);
}
