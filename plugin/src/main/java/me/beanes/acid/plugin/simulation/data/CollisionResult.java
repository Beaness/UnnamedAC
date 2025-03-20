package me.beanes.acid.plugin.simulation.data;

import lombok.Getter;
import lombok.Setter;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

@Getter
@Setter
public final class CollisionResult {
    private SplitStateBoolean collidedX, collidedY, collidedZ;
    private double minimalMotionY, maximumMotionY; // used for slime

    public CollisionResult(SplitStateBoolean collidedX, SplitStateBoolean collidedY, SplitStateBoolean collidedZ, double minimalMotionY, double maximumMotionY) {
        this.collidedX = collidedX;
        this.collidedY = collidedY;
        this.collidedZ = collidedZ;
        this.minimalMotionY = minimalMotionY;
        this.maximumMotionY = maximumMotionY;
    }

    @Override
    public String toString() {
        return "CollisionResult{" +
                "collidedX=" + collidedX +
                ", collidedY=" + collidedY +
                ", collidedZ=" + collidedZ +
                '}';
    }
}
