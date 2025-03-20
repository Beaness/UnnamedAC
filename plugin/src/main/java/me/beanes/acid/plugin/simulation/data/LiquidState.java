package me.beanes.acid.plugin.simulation.data;

import lombok.Getter;

@Getter
public class LiquidState {
    private boolean normalPossible, waterPossible, lavaPossible;

    public LiquidState(boolean normalPossible, boolean waterPossible, boolean lavaPossible) {
        this.normalPossible = normalPossible;
        this.waterPossible = waterPossible;
        this.lavaPossible = lavaPossible;
    }

    public boolean isNormalCertain() {
        return !waterPossible && !lavaPossible;
    }

    public boolean isWaterNotPossible() {
        return normalPossible || lavaPossible;
    }
}
