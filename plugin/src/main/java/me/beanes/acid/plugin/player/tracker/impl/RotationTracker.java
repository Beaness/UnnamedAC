package me.beanes.acid.plugin.player.tracker.impl;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import lombok.Getter;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.player.tracker.Tracker;
import me.beanes.acid.plugin.util.BlockFaces;
import me.beanes.acid.plugin.util.MCMath;

@Getter
public class RotationTracker extends Tracker {
    public RotationTracker(PlayerData data) {
        super(data);
    }
    private float yaw, pitch, lastYaw, lastPitch, deltaYaw, deltaPitch, lastDeltaYaw, lastDeltaPitch;
    public void handleClientTick(WrapperPlayClientPlayerFlying wrapper) {
        this.lastYaw = yaw;
        this.lastPitch = pitch;

        if (wrapper.hasRotationChanged()) {
            this.yaw = wrapper.getLocation().getYaw();
            this.pitch = wrapper.getLocation().getPitch();
        }

        this.lastDeltaYaw = this.deltaYaw;
        this.lastDeltaPitch = this.deltaPitch;

        this.deltaYaw = this.yaw - this.lastYaw;
        this.deltaPitch = this.pitch - this.lastPitch;
    }

    public BlockFace getHorizontalFacing() {
        int index = MCMath.abs_int(MCMath.floor_double((double) (yaw * 4.0F / 360.0F) + 0.5D) & 3) % BlockFaces.HORIZONTAL_PLANE_FACES.length;
        return BlockFaces.HORIZONTAL_PLANE_FACES[index];
    }
}
