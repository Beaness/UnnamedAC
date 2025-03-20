package me.beanes.acid.plugin.player.tracker.impl.velocity;

import com.github.retrooper.packetevents.util.Vector3f;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class TrackedExplosion {
    @Getter
    private final Vector3f velocity;
    @Getter @Setter
    private boolean processed = false;
    @Getter @Setter(value = AccessLevel.PROTECTED)
    private boolean confirmed = false;
    public TrackedExplosion(Vector3f velocity) {
        this.velocity = velocity;
    }
}