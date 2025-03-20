package me.beanes.acid.plugin.player.tracker.impl;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerAbilities;
import lombok.Getter;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.player.tracker.Tracker;
import me.beanes.acid.plugin.util.splitstate.ConfirmableState;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

public class AbilitiesTracker extends Tracker {

    public AbilitiesTracker(PlayerData data) {
        super(data);
    }
    private final ConfirmableState<Boolean> godMode = new ConfirmableState<>(false);
    private final ConfirmableState<Boolean> creativeMode = new ConfirmableState<>(false);
    @Getter
    private final ConfirmableState<Boolean> flightAllowed = new ConfirmableState<>(false);

    public void handleAbilities(WrapperPlayServerPlayerAbilities wrapper) {
        boolean isGodMode = wrapper.isInGodMode();
        boolean isCreativeMode = wrapper.isInCreativeMode();
        boolean allowFly = wrapper.isFlightAllowed();

        godMode.checkTransaction(data);
        creativeMode.checkTransaction(data);
        flightAllowed.checkTransaction(data);

        data.getTransactionTracker().pre(() -> {
            godMode.setValue(isGodMode);
            flightAllowed.setValue(allowFly);
            creativeMode.setValue(isCreativeMode);
        });

        data.getTransactionTracker().post((flightAllowed::confirm));
    }

    public SplitStateBoolean getGodMode() {
        boolean latest = godMode.getValue();
        boolean old = godMode.getOldValue() != null ? godMode.getOldValue() : latest;

        return SplitStateBoolean.result(latest, old);
    }

    public SplitStateBoolean getCreativeMode() {
        boolean latest = creativeMode.getValue();
        boolean old = creativeMode.getOldValue() != null ? creativeMode.getOldValue() : latest;

        return SplitStateBoolean.result(latest, old);
    }

    public SplitStateBoolean i() {
        boolean latest = creativeMode.getValue();
        boolean old = creativeMode.getOldValue() != null ? creativeMode.getOldValue() : latest;

        return SplitStateBoolean.result(latest, old);
    }
}
