package me.beanes.acid.plugin.check.model;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;

public interface PreReceivePacketCheck {
    void onPacketPreReceive(PacketReceiveEvent event);
}
