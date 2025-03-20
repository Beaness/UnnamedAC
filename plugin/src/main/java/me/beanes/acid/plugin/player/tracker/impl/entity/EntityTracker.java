package me.beanes.acid.plugin.player.tracker.impl.entity;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.beanes.acid.plugin.check.impl.cloud.backtrack.BacktrackA;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.player.tracker.Tracker;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

import java.util.Collection;

public class EntityTracker extends Tracker {
    private final Int2ObjectMap<TrackedEntity> entityMap = new Int2ObjectOpenHashMap<>();
    public EntityTracker(PlayerData data) {
        super(data);
    }

    public TrackedEntity getTrackedEntity(int entityId) {
        return entityMap.get(entityId);
    }

    public boolean isTracking(int entityId) {
        return entityMap.containsKey(entityId);
    }

    public EntityType getType(int entityId) {
        return entityMap.containsKey(entityId) ? EntityTypes.PLAYER : null;
    }

    public void handleSpawnPlayer(WrapperPlayServerSpawnPlayer wrapper) {
        data.getTransactionTracker().post(() -> {
            TrackedEntity trackedEntity = new TrackedEntity(wrapper.getPosition(), wrapper.getUUID());
            int entityId = wrapper.getEntityId();

            entityMap.put(entityId, trackedEntity);
        });
    }

    public void handleEntityTeleport(WrapperPlayServerEntityTeleport wrapper) {
        TrackedEntity trackedEntity = getTrackedEntity(wrapper.getEntityId());

        if (trackedEntity == null) {
            return;
        }

        checkForPluginStupidity(trackedEntity);

        Vector3d position = wrapper.getPosition();

        data.getTransactionTracker().pre(() -> {
            trackedEntity.onTeleport(position);
        });

        data.getTransactionTracker().post(trackedEntity::confirm);
    }

    public void handleEntityRelativeMove(WrapperPlayServerEntityRelativeMove wrapper) {
        handleRelativeMove(wrapper.getEntityId(), wrapper.getDeltaX(), wrapper.getDeltaY(), wrapper.getDeltaZ());
    }

    public void handleEntityRelativeMoveAndRotation(WrapperPlayServerEntityRelativeMoveAndRotation wrapper) {
        handleRelativeMove(wrapper.getEntityId(), wrapper.getDeltaX(), wrapper.getDeltaY(), wrapper.getDeltaZ());
    }

    public void handleEntityRotation(WrapperPlayServerEntityRotation wrapper) {
        handleRelativeMove(wrapper.getEntityId(), 0, 0, 0);
    }

    private void handleRelativeMove(int entityId, double deltaX, double deltaY, double deltaZ) {
        TrackedEntity trackedEntity = getTrackedEntity(entityId);

        if (trackedEntity == null) {
            return;
        }

        checkForPluginStupidity(trackedEntity);

        data.getTransactionTracker().pre(() -> {
            trackedEntity.onRelativeMove(deltaX, deltaY, deltaZ);
        });

        data.getTransactionTracker().post(trackedEntity::confirm);
    }

    public void handleUseBed(WrapperPlayServerUseBed wrapper) {
        int entityId = wrapper.getEntityId();

        TrackedEntity trackedEntity = getTrackedEntity(entityId);

        if (trackedEntity == null) {
            return;
        }

        checkForPluginStupidity(trackedEntity);

        // The player box get set to an insanely small box, not worth the implementation, lets mark the player as sleeping

        data.getTransactionTracker().pre(() -> {
            trackedEntity.setSleeping(true);
        });
    }

    public void handleEntityAnimation(WrapperPlayServerEntityAnimation wrapper, PacketSendEvent event) {
        int entityId = wrapper.getEntityId();

        TrackedEntity trackedEntity = getTrackedEntity(entityId);

        if (trackedEntity == null) {
            return;
        }

        if (wrapper.getType() == WrapperPlayServerEntityAnimation.EntityAnimationType.WAKE_UP) {
            // TODO: track entityData for accurate player respawning (position does not really matter as the server sends a teleport right after this packet)
            event.getTasksAfterSend().add(() -> {
                // A post transaction gets hang to it which then restores to a new player object without sleeping = true
                data.getUser().sendPacket(new WrapperPlayServerSpawnPlayer(wrapper.getEntityId(), trackedEntity.getUuid(), new Location(trackedEntity.getServerPos(), 0.0F, 0.0F)));
            });
        }
    }

    public void handleEntityStatus(WrapperPlayServerEntityStatus wrapper) {
        TrackedEntity trackedEntity = getTrackedEntity(wrapper.getEntityId());

        if (trackedEntity == null) {
            return;
        }

        if (wrapper.getStatus() == 2) {
            data.getTransactionTracker().post(() -> {
                trackedEntity.setLastHitAnimationTime(System.currentTimeMillis());
            });
        }
    }

    public void handleEndClientTick() {
        for (final TrackedEntity entity : entityMap.values()) {
            entity.onClientTick();
        }
    }

    private void checkForPluginStupidity(TrackedEntity trackedEntity) {
        // This should never happen because the spigot entity tracker should not send multiple position packets of an entity within the same tick
        // However some dumb plugin could do this by accident so to be safe we keep track of the last transaction
        if (trackedEntity.getLastPreTransaction() == data.getTransactionTracker().getLastTransactionSent()) {
            data.getTransactionTracker().sendTransaction();
        }

        trackedEntity.setLastPreTransaction(data.getTransactionTracker().getLastTransactionSent());
    }

    public Collection<TrackedEntity> getTrackedPlayers() {
        return entityMap.values();
    }


    // TODO: properly make this some day..
    // - Track every entity
    // - Account for uncertainty
    // - Kill my self : (
    public SplitStateBoolean isEntityCollisionPresent(BoundingBox box) {
        if (data.getPositionTracker().getBox().intersectsWith(box)) {
            return SplitStateBoolean.TRUE;
        }

        return SplitStateBoolean.FALSE;

        /* for (TrackedEntity entity : getTrackedPlayers()) {
            // TODO: implemennt bounding boxes for player
        } */
    }
}
