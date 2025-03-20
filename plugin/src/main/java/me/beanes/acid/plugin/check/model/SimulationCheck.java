package me.beanes.acid.plugin.check.model;

import me.beanes.acid.plugin.simulation.data.AreaResult;
import me.beanes.acid.plugin.simulation.data.SimulationResult;

public interface SimulationCheck {

    void onSimulation(SimulationResult result);
}
