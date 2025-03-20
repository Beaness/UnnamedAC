package me.beanes.acid.plugin.player.tracker.impl.velocity;

import com.github.retrooper.packetevents.util.Vector3d;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class TrackedVelocity {
    @Getter
    private final Vector3d velocity;
    @Getter @Setter
    private boolean processed = false;
    @Getter @Setter(value = AccessLevel.PROTECTED)
    private boolean confirmed = false;
    public TrackedVelocity(Vector3d velocity) {
        this.velocity = velocity;
    }
}