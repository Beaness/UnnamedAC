package me.beanes.acid.plugin.player.tracker.impl;

import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.cloud.packet.impl.player.PlayerRequestMitigationPacket;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.player.tracker.Tracker;

public class MitigationRequestTracker extends Tracker {
    public MitigationRequestTracker(PlayerData data) {
        super(data);
    }

    // Limit mitigations request for every 30 seconds
    private long lastMitigationRequest = 0;

    public void requestBlatantMitigation() {
        long delta = System.currentTimeMillis() - lastMitigationRequest;

        if (delta > 30_000) {
            lastMitigationRequest = System.currentTimeMillis();
            Acid.get().getCloudManager().sendPacket(new PlayerRequestMitigationPacket(data.getUser().getUUID()));
        }
    }
}
