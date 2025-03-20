package me.beanes.acid.plugin.player.tracker.impl.position;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.teleport.RelativeFlag;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import lombok.Getter;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.player.tracker.Tracker;
import me.beanes.acid.plugin.util.BoxUtil;
import me.beanes.acid.plugin.util.BoundingBox;
import org.bukkit.Bukkit;

import java.util.ArrayDeque;
import java.util.Queue;

public class PositionTracker extends Tracker {
    public PositionTracker(PlayerData data) {
        super(data);
    }

    @Getter
    private double x, y, z, lastX, lastY, lastZ;
    @Getter
    private double lastReportedX, lastReportedY, lastReportedZ, reportedX, reportedY, reportedZ; // Used for 0.03 stupidity calculation
    @Getter
    private double deltaX, deltaY, deltaZ;
    @Getter
    private boolean lastOnGround, onGround;
    @Getter
    private boolean lastLastTeleport, lastTeleport, teleport = false;
    @Getter
    private boolean lastTeleportSetback, teleportSetback;
    @Getter
    private boolean uncertain = false;
    @Getter
    private boolean lastUncertain = false;
    // All these 4 variables account for teleport 0.03 uncertainty and not giving it when someone got setback
    @Getter
    private boolean uncertainTeleportation, lastUncertainTeleportation = false;
    @Getter
    private boolean uncertainTeleportationSetback, lastUncertainTeleportationSetback;
    @Getter
    private BoundingBox lastBox, box;
    @Getter
    private int clientTicks;
    private final Queue<TeleportData> teleports = new ArrayDeque<>();

    public void handleClientTick(WrapperPlayClientPlayerFlying wrapper) {
        this.lastLastTeleport = this.lastTeleport;
        this.lastTeleport = this.teleport;
        this.lastTeleportSetback = this.teleportSetback;
        this.teleport = false;
        this.teleportSetback = false;
        this.lastUncertain = this.uncertain;
        this.uncertain = !wrapper.hasPositionChanged();

        // Teleport uncertainty shit
        this.lastUncertainTeleportation = uncertainTeleportation;
        this.lastUncertainTeleportationSetback = uncertainTeleportationSetback;
        if (this.uncertain && !this.lastUncertain) { // only allow teleport uncertainty if the teleport was not an anticheat setback
            // If uncertain is uniquely set to true -> set if the uncertain was with teleport
            this.uncertainTeleportation = this.lastTeleport;
            this.uncertainTeleportationSetback = this.lastTeleportSetback;
        } else if (!this.uncertain) {
            // If the player position is no longer uncertain than the teleportation uncertainty is also removed
            this.uncertainTeleportation = false;
        }

        this.lastX = x;
        this.lastY = y;
        this.lastZ = z;

        if (!this.uncertain) {
            this.x = wrapper.getLocation().getX();
            this.y = wrapper.getLocation().getY();
            this.z = wrapper.getLocation().getZ();

            // Check if this move was a teleport
            if (wrapper.hasRotationChanged()) {
                TeleportData teleportData = this.teleports.peek();

                if (teleportData != null) {
                    Location teleportLocation = teleportData.getLocation();

                    // 1.7.10 deals onGround teleport different
                    boolean groundCheck = !wrapper.isOnGround() || data.getUser().getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_7_10);

                    if (teleportLocation.getX() == this.x &&
                            teleportLocation.getY() == this.y &&
                            teleportLocation.getZ() == this.z &&
                            (teleportData.isRelativeYaw() || teleportLocation.getYaw() == wrapper.getLocation().getYaw()) &&
                            (teleportData.isRelativePitch() || teleportLocation.getPitch() == wrapper.getLocation().getPitch()) &&
                            groundCheck
                    ) {
                        if (teleportData.isSetback()) {
                            data.getSetbackTracker().confirmSetbackTeleport();
                        }

                        this.teleports.remove();
                        this.teleport = true;
                        this.teleportSetback = teleportData.isSetback();
                    }
                }
            }
        }

        // If the client teleported we can't trust the updated onGround value (the client sends onGround state false on a teleport)
        if (!this.teleport) {
            this.lastOnGround = this.onGround;
            this.onGround = wrapper.isOnGround();

            this.clientTicks++;

            this.lastReportedX = this.reportedX;
            this.lastReportedY = this.reportedY;
            this.lastReportedZ = this.reportedZ;

            if (wrapper.hasPositionChanged()) {
                this.reportedX = wrapper.getLocation().getX();
                this.reportedY = wrapper.getLocation().getY();
                this.reportedZ = wrapper.getLocation().getZ();
            }
        }

        this.deltaX = this.x - this.lastX;
        this.deltaY = this.y - this.lastY;
        this.deltaZ = this.z - this.lastZ;

        this.lastBox = box;
        this.box = BoxUtil.getPlayerBox(this.x, this.y, this.z, data.getPositionTracker().isUncertain() ? 0.03D : 0D);

        // Set last box to first box if player just joined
        if (this.lastBox == null) {
            this.lastBox = box;
        }
    }

    public void handleServerTeleport(WrapperPlayServerPlayerPositionAndLook wrapper) {
        RelativeFlag relativeFlag = wrapper.getRelativeFlags();

        // We remove relative XYZ flags

        if (relativeFlag.has(RelativeFlag.X.getMask())) {
            wrapper.setX(this.x + wrapper.getX());
        }

        if (relativeFlag.has(RelativeFlag.Y.getMask())) {
            wrapper.setY(this.y + wrapper.getX());
        }

        if (relativeFlag.has(RelativeFlag.Z.getMask())) {
            wrapper.setZ(this.z + wrapper.getZ());
        }

        boolean yawRel = relativeFlag.has(RelativeFlag.YAW.getMask());
        boolean pitchRel = relativeFlag.has(RelativeFlag.PITCH.getMask());
        int newMask = (yawRel ? 0 : RelativeFlag.YAW.getMask()) & (pitchRel ? 0 : RelativeFlag.PITCH.getMask());

        RelativeFlag newFlag = new RelativeFlag(newMask);
        wrapper.setRelativeFlags(newFlag);

        double x = wrapper.getX();
        double y = wrapper.getY();
        double z = wrapper.getZ();

        double dX = x - this.x;
        double dY = y - this.y;
        double dZ = z - this.z;

        // Use 0.03 as magic value (because mojang also did this haha) to send another transaction to hook the teleport to
        if (dX * dX + dY * dY + dZ * dZ < 9.0E-4D) {
            data.getTransactionTracker().sendTransaction();
        }

        // Register a teleport as a safe position
        data.getSetbackTracker().registerSafePosition(x, y, z);

        Location pos = new Location(x, y, z, wrapper.getYaw() % 360.0F, wrapper.getPitch() % 360.0F); // We have to do % 360.0F because the client also does this
        data.getTransactionTracker().pre(() -> {
            // The pre transaction is needed here to do 2 things: only allow the player to accept a tp if it has accepted the trans first + reduce false flags for very small tps next to a player position (this can cause falsing)
            this.teleports.add(new TeleportData(pos, yawRel, pitchRel, false));
        });
    }

    public void addSetbackToTeleportQueue(double x, double y, double z) {
        this.teleports.add(new TeleportData(new Location(x, y, z, 0F, 0F), true, true, true));
    }

    public boolean isTeleportQueued() {
        return !this.teleports.isEmpty();
    }
}
