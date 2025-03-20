package me.beanes.acid.plugin.simulation.processor.impl.move;

import it.unimi.dsi.fastutil.floats.FloatCollection;
import me.beanes.acid.plugin.player.PlayerData;

public abstract class MoveGetter {
    protected final PlayerData data;

    public MoveGetter(PlayerData data) {
        this.data = data;
    }
    public abstract void addMovementFactors(FloatCollection factors, FloatCollection sprintFactors);

    protected double[] getPossibleMoveSpeeds(boolean onlySprint) {
        if (onlySprint) {
            if (data.getAttributeTracker().getMovementSpeedWithSprint().getOldValue() != null) {
                return new double[]{ data.getAttributeTracker().getMovementSpeedWithSprint().getValue(), data.getAttributeTracker().getMovementSpeedWithSprint().getOldValue() };
            } else {
                return new double[]{ data.getAttributeTracker().getMovementSpeedWithSprint().getValue() };
            }
        } else {
            if (data.getAttributeTracker().getMovementSpeed().getOldValue() != null) {
                return new double[]{ data.getAttributeTracker().getMovementSpeed().getValue(), data.getAttributeTracker().getMovementSpeed().getOldValue() };
            } else {
                return new double[]{ data.getAttributeTracker().getMovementSpeed().getValue() };
            }
        }
    }
}
