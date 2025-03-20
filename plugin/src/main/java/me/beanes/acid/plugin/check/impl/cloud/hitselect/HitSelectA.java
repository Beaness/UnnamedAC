package me.beanes.acid.plugin.check.impl.cloud.hitselect;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import me.beanes.acid.plugin.check.model.CloudCheck;
import me.beanes.acid.plugin.check.model.ReceivePacketCheck;
import me.beanes.acid.plugin.check.model.SendPacketCheck;
import me.beanes.acid.plugin.check.model.SimulationCheck;
import me.beanes.acid.plugin.cloud.packet.impl.check.impl.HitSelectTimesPacket;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.player.tracker.impl.entity.TrackedEntity;
import me.beanes.acid.plugin.simulation.data.SimulationResult;
import me.beanes.acid.plugin.util.CircularIntSampler;

public class HitSelectA extends CloudCheck implements ReceivePacketCheck, SendPacketCheck {

    private static final int SAMPLE_SIZE = 20;
    private int targetEntity = Integer.MIN_VALUE;
    private int ticksSinceHurt = 0;
    private final CircularIntSampler delayTimes = new CircularIntSampler(SAMPLE_SIZE);
    private int queuedValue;

    public HitSelectA(PlayerData data) {
        super(data, "HitSelectA");
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);

            if (wrapper.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                return;
            }

            int entityId = wrapper.getEntityId();

            if (data.getEntityTracker().getType(entityId) != EntityTypes.PLAYER) {
                return;
            }

            // Switch target
            if (targetEntity != entityId) {
                targetEntity = entityId;
                ticksSinceHurt = 21;
                queuedValue = Integer.MIN_VALUE;
                return;
            }

            if (ticksSinceHurt > 20) {
                queuedValue = Integer.MIN_VALUE;
                return;
            }


            queuedValue = ticksSinceHurt;
        } else if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            ticksSinceHurt++;

            if (targetEntity != Integer.MIN_VALUE && !data.getPositionTracker().isUncertain()) {
                TrackedEntity trackedEntity = data.getEntityTracker().getTrackedEntity(targetEntity);

                if (trackedEntity != null) {
                    double lastDistance = trackedEntity.getPosition().getDistanceXZ(data.getPositionTracker().getLastX(), data.getPositionTracker().getLastY(), data.getPositionTracker().getLastZ());
                    double newDistance = trackedEntity.getPosition().getDistanceXZ(data.getPositionTracker().getX(), data.getPositionTracker().getY(), data.getPositionTracker().getZ());

                    if (newDistance > lastDistance) {
                        debug("moved away");
                        targetEntity = Integer.MIN_VALUE;
                        return;
                    }
                }
            };

            if (queuedValue != Integer.MIN_VALUE) {
                delayTimes.fill(queuedValue);
                debug("ticksSinceHurt = " + queuedValue);
                queuedValue = Integer.MIN_VALUE;
            }
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.ENTITY_STATUS) {
            WrapperPlayServerEntityStatus wrapper = new WrapperPlayServerEntityStatus(event);
            if (wrapper.getEntityId() == targetEntity && wrapper.getStatus() == 2) {
                data.getTransactionTracker().post(() -> {
                    ticksSinceHurt = 0;
                });

                event.getTasksAfterSend().add(() -> data.getTransactionTracker().sendTransaction());
            }
        }
    }
}
