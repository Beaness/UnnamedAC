package me.beanes.acid.plugin.simulation.processor.impl.move;

import it.unimi.dsi.fastutil.floats.FloatCollection;
import me.beanes.acid.plugin.player.PlayerData;

public class LavaMove extends MoveGetter {
    public LavaMove(PlayerData data) {
        super(data);
    }
    @Override
    public void addMovementFactors(FloatCollection factors, FloatCollection sprintFactors) {
        factors.add(0.02F);
        // Lava is a constant inertia, we hardcode it
    }
}
