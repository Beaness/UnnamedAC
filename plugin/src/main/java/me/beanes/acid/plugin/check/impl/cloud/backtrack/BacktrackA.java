package me.beanes.acid.plugin.check.impl.cloud.backtrack;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMoveAndRotation;
import me.beanes.acid.plugin.check.model.CloudCheck;
import me.beanes.acid.plugin.check.model.ReceivePacketCheck;
import me.beanes.acid.plugin.check.model.SendPacketCheck;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.player.tracker.impl.entity.TrackedEntity;
import me.beanes.acid.plugin.util.CircularIntSampler;
import me.beanes.acid.plugin.util.VectorUtils;

import java.util.concurrent.atomic.AtomicReference;

public class BacktrackA extends CloudCheck implements ReceivePacketCheck, SendPacketCheck {
    private static final int SAMPLE_SIZE = 20;
    private static final double MINIMAL_DISTANCE_FROM_PLAYER = 0.75D;
    private static final double AWAY_MAXIMUM_DISTANCE_FROM_PLAYER = 5.5D;
    private static final double MINIMAL_MOVE = 0.15D;
    private static final long MAXIMUM_HIT_AWAY_TIME = 2_500;
    private static final int MAXIMUM_AWAY_TIMES = 3;

    public BacktrackA(PlayerData data) {
        super(data, "BacktrackA");
    }

    private int targetEntity = Integer.MIN_VALUE;
    private long hitTime = 0;
    private int awayTimesProcessed = 0;
    private final CircularIntSampler awayTimes = new CircularIntSampler(SAMPLE_SIZE);
    private final CircularIntSampler closerTimes = new CircularIntSampler(SAMPLE_SIZE);

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);

            if (wrapper.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                targetEntity = wrapper.getEntityId();
                hitTime = System.currentTimeMillis();
                awayTimesProcessed = 0;
            }
        };
    }


    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.ENTITY_RELATIVE_MOVE) {
            WrapperPlayServerEntityRelativeMove wrapper = new WrapperPlayServerEntityRelativeMove(event);

            onDeltaMovement(wrapper.getEntityId(), wrapper.getDeltaX(), wrapper.getDeltaY(), wrapper.getDeltaZ());
        } else if (event.getPacketType() == PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION) {
            WrapperPlayServerEntityRelativeMoveAndRotation wrapper = new WrapperPlayServerEntityRelativeMoveAndRotation(event);

            onDeltaMovement(wrapper.getEntityId(), wrapper.getDeltaX(), wrapper.getDeltaY(), wrapper.getDeltaZ());
        }
    }

    public void onDeltaMovement(int entityId, double x, double y, double z) {
        if (x == 0 && y == 0 && z == 0) {
            return;
        }

        if (entityId != targetEntity) {
            return;
        }

        // debug("delta movement it seems");

        TrackedEntity entity = data.getEntityTracker().getTrackedEntity(entityId);

        if (entity == null) {
            return;
        }

        if (!entity.getServerBase().isCertain()) {
            return;
        }

        // The position we get now is not updated yet, so we can use this as "original" position
        Vector3d originalPosition = entity.getServerPos();

        AtomicReference<MovementType> typeRef = new AtomicReference<>();

        // debug("getting ready!");

        // TODO: cancel close if just away

        // This task is ran after the entity tracker updated the serverPos
        data.getTransactionTracker().pre(() -> {
            Vector3d self = new Vector3d(data.getPositionTracker().getX(), data.getPositionTracker().getY(), data.getPositionTracker().getZ());
            Vector3d newPosition = entity.getServerPos();

            // Could technically use squared distance to compare but who cares :P
            double distanceToNewPosition = self.distance(newPosition);
            double distanceToOriginalPosition = self.distance(originalPosition);
            double diffXZ = VectorUtils.getDistanceXZ(originalPosition, newPosition);

            // Do not trust mini movements on XZ axis
            if (diffXZ < MINIMAL_MOVE) {
                typeRef.set(MovementType.USELESS);
                return;
            }

            // debug("diffXZ=" + diffXZ);

            // Do not trust if the enemy too close to player (away / close could be messed up)
            if (distanceToOriginalPosition < MINIMAL_DISTANCE_FROM_PLAYER) {
                typeRef.set(MovementType.USELESS);
                return;
            }

            boolean away = distanceToNewPosition > distanceToOriginalPosition;

            if (away) {
                // debug("detectaway");
                // Only trust away movements closeby
                if (distanceToOriginalPosition > AWAY_MAXIMUM_DISTANCE_FROM_PLAYER || distanceToNewPosition > AWAY_MAXIMUM_DISTANCE_FROM_PLAYER) {
                    typeRef.set(MovementType.USELESS);
                } else {
                    // We can only process a limit amount of aways per time & limited amount of time after a hit
                    if (awayTimesProcessed >= MAXIMUM_AWAY_TIMES || ((System.currentTimeMillis() - hitTime) > MAXIMUM_HIT_AWAY_TIME)) {
                        typeRef.set(MovementType.USELESS);
                    } else {
                        typeRef.set(MovementType.AWAY);
                        awayTimesProcessed++;
                    }
                }
            } else {
                // We can always trust closer
                typeRef.set(MovementType.CLOSER);
            }

            // debug("distanceToNewPosition=" + distanceToNewPosition + " distanceToOriginalPosition=" + distanceToOriginalPosition + " diff=" + diffXZ + " awayTimesProcessed=" + awayTimesProcessed);


            /* // Backtrack might not have triggered
            if (distanceToNewPosition > MAXIMUM_DISTANCE_FROM_PLAYER || distanceToOriginalPosition < MINIMAL_DISTANCE_FROM_PLAYER) {
                typeRef.set(MovementType.USELESS);
                return;
            }

            if (diff > MINIMAL_MOVE) {
                // The movement was away
                typeRef.set(MovementType.AWAY);
            } else if (diff < -MINIMAL_MOVE) {
                // The movement was closer
                typeRef.set(MovementType.CLOSER);
            } else {
                // The movement was too small
                typeRef.set(MovementType.USELESS);
            } */

        });

        data.getTransactionTracker().post(responseTime -> {
            MovementType type = typeRef.get();

            if (type == MovementType.AWAY) {
                // debug("away=" + responseTime);
                awayTimes.fill(responseTime);
            } else if (type == MovementType.CLOSER) {
                // debug("close=" + responseTime);
                closerTimes.fill(responseTime);
            }
        });

        // sendToCloud(new BacktrackTimesCheckPacket());
    }

    private enum MovementType {
        AWAY, CLOSER, USELESS
    }
}
