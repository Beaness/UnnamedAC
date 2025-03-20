package me.beanes.acid.plugin.simulation.processor;

import me.beanes.acid.plugin.player.PlayerData;

public abstract class AreaProcessor {
    protected final PlayerData data;

    public AreaProcessor(PlayerData data) {
        this.data = data;
    }
}
