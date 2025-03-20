package me.beanes.acid.plugin.player.tracker.impl.velocity;

import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerExplosion;
import lombok.Getter;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.player.tracker.Tracker;
import me.beanes.acid.plugin.util.splitstate.ConfirmableState;

public class VelocityTracker extends Tracker {
    public VelocityTracker(PlayerData data) {
        super(data);
    }

    @Getter
    private final ConfirmableState<TrackedVelocity> trackedVelocity = new ConfirmableState<>(new TrackedVelocity(new Vector3d(0, 0, 0), true, true));
    @Getter
    private final ConfirmableState<TrackedExplosion> trackedExplosion = new ConfirmableState<>(new TrackedExplosion(new Vector3f(0, 0, 0), true, true));

    public void handleVelocity(WrapperPlayServerEntityVelocity wrapper) {
        if (wrapper.getEntityId() == data.getUser().getEntityId()) {
            trackedVelocity.checkTransaction(data);

            Vector3d vector = wrapper.getVelocity();

            data.getTransactionTracker().pre(() -> {
                trackedVelocity.setValue(new TrackedVelocity(vector));
            });

            data.getTransactionTracker().post(() -> {
                trackedVelocity.getValue().setConfirmed(true);
                trackedVelocity.confirm();
            });
        }
    }

    public void handleExplosion(WrapperPlayServerExplosion wrapper) {
        trackedExplosion.checkTransaction(data);

        Vector3f vector = wrapper.getPlayerMotion(); // This seems to do float <-> double conversion, check for precision errors...

        data.getTransactionTracker().pre(() -> {
            trackedExplosion.setValue(new TrackedExplosion(vector));
        });

        data.getTransactionTracker().post(() -> {
            trackedExplosion.getValue().setConfirmed(true);
            trackedExplosion.confirm();
        });
    }
}
