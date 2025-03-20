package me.beanes.acid.plugin.player.tracker.impl.position;

import com.github.retrooper.packetevents.protocol.teleport.RelativeFlag;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.player.tracker.Tracker;

public class SetbackTracker extends Tracker {

    public SetbackTracker(PlayerData data) {
        super(data);
    }

    private static final byte FLAGS_RELATIVE_ROTATION = (byte) (RelativeFlag.YAW.getMask() | RelativeFlag.PITCH.getMask());

    // TODO: cancel any movement input until player accepted setback

    private int setbackTicks = 0;
    private double safeX, safeY, safeZ;

    public void handleClientTick(WrapperPlayClientPlayerFlying wrapper) {
        // Don't allow the client to send movement updates
        if (data.getPositionTracker().isTeleportQueued()) {
            wrapper.setPositionChanged(false);
        }
    }

    public void setback() {
        if (data.getPositionTracker().isTeleportQueued()) {
            return;
        }

        data.getUser().sendPacketSilently(new WrapperPlayServerPlayerPositionAndLook(safeX, safeY, safeZ, 0, 0, FLAGS_RELATIVE_ROTATION, 0, false));
        data.getPositionTracker().addSetbackToTeleportQueue(safeX, safeY, safeZ);
    }

    public void registerSafePositionIfPossible(double x, double y, double z) {
        // Do not register a safe position if a teleport is inbound, this could be anything from server teleport or setback teleport
        if (data.getPositionTracker().isTeleportQueued()) {
            return;
        }

        registerSafePosition(x, y, z);
    }

    public void registerSafePosition(double x, double y, double z) {
        this.safeX = x;
        this.safeY = y;
        this.safeZ = z;
    }

    public void doVelocity(double x, double y, double z) {

    }

    public void confirmSetbackTeleport() {
        setbackTicks--;
    }

}
