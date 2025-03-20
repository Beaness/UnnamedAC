package me.beanes.acid.plugin.player.tracker.impl.position;

import com.github.retrooper.packetevents.protocol.world.Location;
import lombok.Getter;

@Getter
public class TeleportData {
    private final Location location;
    private final boolean setback;
    private final boolean relativeYaw, relativePitch;

    public TeleportData(Location location, boolean relativeYaw, boolean relativePitch, boolean setback) {
        this.location = location;
        this.relativeYaw = relativeYaw;
        this.relativePitch = relativePitch;
        this.setback = setback;
    }
}
