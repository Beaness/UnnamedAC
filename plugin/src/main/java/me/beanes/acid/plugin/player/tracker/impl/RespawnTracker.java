package me.beanes.acid.plugin.player.tracker.impl;

import lombok.Getter;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.player.tracker.Tracker;

public class RespawnTracker extends Tracker {
    public RespawnTracker(PlayerData data) {
        super(data);
    }

    @Getter
    private boolean possibleRespawning;
    private boolean reset = false;
    public void handleRespawn() {
        data.getTransactionTracker().pre(() -> {
            possibleRespawning = true;
        });

        data.getTransactionTracker().post(() -> {
            reset = true;
        });
    }

    public void handleEndClientTick() {
        if (reset) {
            this.reset = false;
            this.possibleRespawning = false;
        }
    }
}
