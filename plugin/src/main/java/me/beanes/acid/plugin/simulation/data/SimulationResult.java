package me.beanes.acid.plugin.simulation.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

@Getter
@AllArgsConstructor
public class SimulationResult {
    private boolean correct;
    private LiquidState liquidState;
    private boolean processedVelocity;
    private SplitStateBoolean jumped;
}
