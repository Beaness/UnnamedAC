package me.beanes.acid.plugin.player.tracker.impl.entity;

import com.github.retrooper.packetevents.util.Vector3d;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;

import java.util.UUID;

// This used to be the hardcoded splitting tracking but that ain't production ready so its area based (dusk implementation im sorry I skidded I already spent too much time on other stuff and im getting lazy)
public class TrackedEntity {
    private static final double PRECISION_ERROR = 0.0D;
    private static final double MIN_TELEPORT_HORIZONTAL = 0.03125D - PRECISION_ERROR, MIN_TELEPORT_VERTICAL = 0.015625D - PRECISION_ERROR;
    @Getter
    private UUID uuid;
    @Getter
    private Vector3d serverPos;
    @Getter
    private final EntityArea serverBase;
    private final EntityArea target;
    @Getter
    private final EntityArea position;
    @Getter @Setter
    private int lastPreTransaction;

    private boolean confirmed = true;
    private int interpolation = 0;

    @Getter @Setter
    private long lastHitAnimationTime = 0;
    @Getter @Setter
    private boolean sleeping;

    public TrackedEntity(Vector3d pos, UUID uuid) {
        this.serverPos = pos;

        this.serverBase = new EntityArea(pos);
        this.target = new EntityArea(pos);
        this.position = new EntityArea(pos);

        this.uuid = uuid;
    }

    public void onTeleport(Vector3d position) {
        double x = position.getX();
        double y = position.getY();
        double z = position.getZ();

        this.serverPos = new Vector3d(x, y, z);
        this.serverBase.set(x, y, z);
        this.target.allow(x, y, z);

        // If the distance is too close to the client position it is possible for the base to remain unchanged
        if (this.position.distanceX(x) < MIN_TELEPORT_HORIZONTAL && this.position.distanceY(y) < MIN_TELEPORT_VERTICAL && this.position.distanceZ(z) < MIN_TELEPORT_HORIZONTAL) {
            this.serverBase.expand(MIN_TELEPORT_HORIZONTAL, MIN_TELEPORT_VERTICAL, MIN_TELEPORT_HORIZONTAL);
            this.target.expand(MIN_TELEPORT_HORIZONTAL, MIN_TELEPORT_VERTICAL, MIN_TELEPORT_HORIZONTAL);
        }

        this.resetInterpolation();
    }

    public void onRelativeMove(double x, double y, double z) {
        this.serverPos = serverPos.add(x, y, z);

        double newX = serverPos.getX();
        double newY = serverPos.getY();
        double newZ = serverPos.getZ();

        this.serverBase.set(newX, newY, newZ);
        this.target.allow(newX, newY, newZ);

        this.resetInterpolation();
    }

    public void confirm() {
        // The client has processed the last new movement
        this.confirmed = true;
        this.target.set(this.serverBase);
    }

    public void onClientTick() {
        if (this.confirmed) {
            if (this.interpolation > 0) {
                this.position.interpolate(this.target, this.interpolation--);
            }
        } else {
            // (this is the fun part!)
            // So at this state we are not sure if the client received the new position
            // The problem is if the client has not received it we need to account for eventual interpolation
            // So we just allow the whole interpolation :(
            this.position.allow(this.target);
        }
    }

    private void resetInterpolation() {
        this.confirmed = false;
        this.interpolation = 3;
    }
}
