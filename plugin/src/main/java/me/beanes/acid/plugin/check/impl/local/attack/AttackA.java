package me.beanes.acid.plugin.check.impl.local.attack;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.sound.Sound;
import com.github.retrooper.packetevents.protocol.sound.StaticSound;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.Getter;
import me.beanes.acid.plugin.check.model.LocalCheck;
import me.beanes.acid.plugin.check.model.ReceivePacketCheck;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.player.tracker.impl.entity.EntityArea;
import me.beanes.acid.plugin.player.tracker.impl.entity.TrackedEntity;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.BoxUtil;
import me.beanes.acid.plugin.util.InterceptData;
import me.beanes.acid.plugin.util.MCMath;
import me.beanes.acid.plugin.util.trig.TrigHandler;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AttackA extends LocalCheck implements ReceivePacketCheck {

    // 1.62 is normal eight height, 1.62 - 0.08 for sneaking
    private static final double[] EYE_HEIGHTS = new double[] {(double) (1.62f), (double) (1.62f - 0.08f)};

    // Theres multiple float / double inaccuracies possible (for example the position itself of the player is inaccurate sometimes)
    // So have a leniency which is barely anything ingame to account for those inaccuracies
    private static final double LENIENCY = 0.00001D;

    // The check should be mathematically correct but.. just to be safe.. keep a leniency until I can test this properly and extensively
    private static final double PIERCING_LENIENCY = 0.1D;

    // The 1.8.9 hurt sound effect to play on cancelled reach hits
    private static final Sound HURT_SOUND = new StaticSound(ResourceLocation.minecraft("game.player.hurt"), null);


    private final Int2ObjectMap<Vector3d> attacks = new Int2ObjectLinkedOpenHashMap<>();

    public AttackA(PlayerData data) {
        super(data, "AttackA");
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            if (!data.getPositionTracker().isTeleport() && !attacks.isEmpty()) {
                this.processAttacks();
            }
        }

        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
            if (wrapper.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                attacks.put(wrapper.getEntityId(), new Vector3d(data.getPositionTracker().getX(), data.getPositionTracker().getY(), data.getPositionTracker().getZ()));

                if (shouldCancel(wrapper.getEntityId())) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private boolean shouldCancel(int entityId) {
        if (data.getStateTracker().isCreative().possible()) {
            return false;
        }

        final TrackedEntity trackedEntity = data.getEntityTracker().getTrackedEntity(entityId);

        if (trackedEntity == null) {
            return false;
        }

        if (trackedEntity.isSleeping()) {
            return false;
        }

        EntityArea area = trackedEntity.getPosition();
        BoundingBox box = createBiggestBox(area, data.getPositionTracker().isUncertain());

        double closestDistance = Double.MAX_VALUE;

        for (final double eyeHeight : EYE_HEIGHTS) {
            Vector3d from = new Vector3d(data.getPositionTracker().getX(), data.getPositionTracker().getY() + eyeHeight, data.getPositionTracker().getZ());
            closestDistance = Math.min(box.getClosestDistance(from), closestDistance);
        }

        boolean cancel = closestDistance > 3.0D;

        if (cancel) {
            debug("closestDistance=" + closestDistance);

            // Do a fake hit lol if it has been 500 ms
            long delta = System.currentTimeMillis() - trackedEntity.getLastHitAnimationTime();

            if (delta > 400) {
                trackedEntity.setLastHitAnimationTime(System.currentTimeMillis());

                data.getUser().sendPacket(new WrapperPlayServerEntityStatus(entityId, 2));
                data.getUser().sendPacket(new WrapperPlayServerSoundEffect(
                        HURT_SOUND,
                        null,
                        new Vector3i((int) (data.getPositionTracker().getX() * 8.0D), (int) (data.getPositionTracker().getY() * 8.0D), (int) (data.getPositionTracker().getZ() * 8.0D)),
                        1.0F,
                        0.992126F)
                );
            }
        }

        return cancel;
    }

    private void processAttacks() {
        if (data.getStateTracker().isCreative().possible()) {
            attacks.clear();
            return;
        }

        boolean uncertain = data.getPositionTracker().isLastUncertain();
        Vector3d[] lookVectors = getLookVectors();

        try {
            for (Map.Entry<Integer, Vector3d> attack : attacks.entrySet()) {
                final Vector3d from = attack.getValue();
                final TrackedEntity trackedEntity = data.getEntityTracker().getTrackedEntity(attack.getKey());

                if (trackedEntity != null && !trackedEntity.isSleeping()) {
                    processAttack(from, trackedEntity, uncertain, lookVectors);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        attacks.clear();
    }

    private void processAttack(Vector3d from, TrackedEntity trackedEntity, boolean uncertain, Vector3d[] lookVectors) {
        double reach = Double.MAX_VALUE;

        EntityArea area = trackedEntity.getPosition();
        BoundingBox box = createBiggestBox(area, uncertain);

        List<PiercingTestBox> piercingTestBoxes = new ArrayList<>();

        double selfX = data.getPositionTracker().getX();
        double selfY = data.getPositionTracker().getY();
        double selfZ = data.getPositionTracker().getZ();

        for (TrackedEntity entity : data.getEntityTracker().getTrackedPlayers()) {
            EntityArea blockArea = entity.getPosition();

            if (entity == trackedEntity) continue;

            // The position is certain
            if (blockArea.isCertain()) {
                double distX = (selfX - blockArea.minX) * (selfX - blockArea.minX);
                double distY = (selfY - blockArea.minY) * (selfY - blockArea.minY);
                double distZ = (selfZ - blockArea.minZ) * (selfZ - blockArea.minZ);

                // If a tracked player is in a 6 block range -> add pierce test boxes
                if (distX * distX + distY * distY + distZ * distZ <= 36.0D) {
                    piercingTestBoxes.add(new PiercingTestBox(createSmallestBox(blockArea, uncertain), createBiggestBox(blockArea, uncertain), entity.getUuid()));
                }
            };
        }

        // If eye vec is within a box of an entity
        // the best reach value (to filter to lowest entity) is set to zero
        // but when bestReach=0 any entity will be picked
        // so if you have this order:
        // ENTITY IN UR EYE VEC | ENTITY 3 BLOCKS AWAY
        // You will actually hit the entity 3 blocks away
        // Gotta love minecraft protocol...

        boolean hitInsideEntitySoNoPiercingFucked = false;

        outer: for (final Vector3d lookVec : lookVectors) {
            for (final double eye : EYE_HEIGHTS) {
                Vector3d startReach = new Vector3d(from.getX(), from.getY() + eye, from.getZ());
                Vector3d endReach = startReach.add(lookVec.getX() * 6, lookVec.getY() * 6, lookVec.getZ() * 6);

                if (box.isVecInside(startReach)) {
                    reach = 0;
                    hitInsideEntitySoNoPiercingFucked = true;
                    break outer;
                } else {
                    InterceptData intercept = box.calculateIntercept(startReach, endReach);

                    if (intercept != null) {
                        double dist = startReach.distance(intercept.getVector());
                        reach = Math.min(dist, reach);

                        // As long as no hit was inside another entity's box, we can keep checking for piercing
                        if (!hitInsideEntitySoNoPiercingFucked) {
                            BoundingBox selectingBox = BoxUtil.getPlayerBox(from.getX(), from.getY(), from.getZ(), 0.0D).addCoord(lookVec.getX() * 4.5F, lookVec.getY() * 4.5F, lookVec.getZ() * 4.5F).expand(1.0F, 1.0F, 1.0F).expand(-LENIENCY, -LENIENCY, -LENIENCY); // Leniency for player box errors

                            // For every valid ray to the target, check possible entities that the player might have gone through
                            // Calculate the max distance to these entities
                            for (PiercingTestBox piercingTestBox : piercingTestBoxes) {
                                // First do the retarded mc test because this can fuck up the whole reach ordering
                                if (piercingTestBox.getBigBox().isVecInside(startReach)) {
                                    hitInsideEntitySoNoPiercingFucked = true;
                                    continue outer;
                                }

                                if (piercingTestBox.isMissed()) {
                                    continue;
                                }

                                if (!piercingTestBox.getSmallBox().intersectsWith(selectingBox)) {
                                    // Didn't possibly get selected
                                    piercingTestBox.mightHaveMissed();
                                    continue;
                                }

                                InterceptData otherIntercept = piercingTestBox.getSmallBox().calculateIntercept(startReach, endReach);

                                if (otherIntercept == null) {
                                    // The intercept might have missed the box
                                    piercingTestBox.mightHaveMissed();
                                } else {
                                    double otherDist = startReach.distance(otherIntercept.getVector());

                                    if (otherDist > dist) {
                                        piercingTestBox.mightHaveMissed();
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }

        if (reach > 3) {
            debug("from=§a" + from);
            debug("uncertain=§a" + uncertain);
            debug("area=§a" + area);

            Document logData = new Document()
                .append("distance", reach)
                .append("from", from)
                .append("uncertain", uncertain)
                .append("lookVectors", lookVectors)
                .append("area", area);

            data.getMitigationRequestTracker().requestBlatantMitigation();

            log(logData);
            certainAlert(reach == Double.MAX_VALUE ? "HitBoxes" : "Reach");
        } else if (!hitInsideEntitySoNoPiercingFucked) {
            boolean pierced = false;

            for (PiercingTestBox testBox : piercingTestBoxes) {
                if (!testBox.isMissed()) {
                    debug("pierced=" + testBox.getUuid());
                    pierced = true;
                }
            }

            if (pierced) {
                log(new Document());

                debug("hit=" + trackedEntity.getUuid());
                debug("hitReach=" + reach);

                certainAlert("Piercing");
            }
        }
    }


    private BoundingBox createBiggestBox(EntityArea area, boolean uncertain) {
        BoundingBox box = new BoundingBox(area.minX, area.minY, area.minZ, area.maxX, area.maxY, area.maxZ)
                .expand(BoxUtil.PLAYER_WIDTH / 2.0, 0.0, BoxUtil.PLAYER_WIDTH / 2.0)
                .addCoord(0.0, BoxUtil.PLAYER_HEIGHT, 0.0)
                .expand(0.1F, 0.1F, 0.1F)
                .expand(LENIENCY, LENIENCY, LENIENCY);

        if (uncertain) {
            box = box.expand(0.03D, 0.03D, 0.03D);
        }

        return box;
    }

    private BoundingBox createSmallestBox(EntityArea area, boolean uncertain) {
        BoundingBox box = new BoundingBox(area.minX, area.minY, area.minZ, area.maxX, area.maxY, area.maxZ)
                .expand(BoxUtil.PLAYER_WIDTH / 2.0, 0.0, BoxUtil.PLAYER_WIDTH / 2.0)
                .addCoord(0.0, BoxUtil.PLAYER_HEIGHT, 0.0)
                .expand(0.1F, 0.1F, 0.1F)
                .expand(-PIERCING_LENIENCY, -PIERCING_LENIENCY, -PIERCING_LENIENCY); // Leniency negative as we want the smallest box

        // Scale downwards due to 0.03 uncertainty
        if (uncertain) {
            box = box.expand(-0.03D, -0.03D, -0.03D);
        }

        return box;
    }

    private Vector3d[] getLookVectors() {
        Vector3d[] lookVectors;
        if (data.getUser().getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_8)) {
            // Older optifine versions of 1.8 have the legacy fast math too, super fun!
            lookVectors = new Vector3d[]{
                    MCMath.getVectorForRotation(TrigHandler.VANILLA_MATH, data.getRotationTracker().getPitch(), data.getRotationTracker().getYaw()),
                    MCMath.getVectorForRotation(TrigHandler.FAST_MATH, data.getRotationTracker().getPitch(), data.getRotationTracker().getYaw()),
                    MCMath.getVectorForRotation(TrigHandler.LEGACY_FAST_MATH, data.getRotationTracker().getPitch(), data.getRotationTracker().getYaw()),
                    // Vanilla uses the last yaw -> a lot of clients & mods have this fixed though
                    // Read more: https://github.com/prplz/MouseDelayFix
                    MCMath.getVectorForRotation(TrigHandler.VANILLA_MATH, data.getRotationTracker().getPitch(), data.getRotationTracker().getLastYaw()),
                    MCMath.getVectorForRotation(TrigHandler.FAST_MATH, data.getRotationTracker().getPitch(), data.getRotationTracker().getLastYaw()),
                    MCMath.getVectorForRotation(TrigHandler.LEGACY_FAST_MATH, data.getRotationTracker().getPitch(), data.getRotationTracker().getLastYaw())
            };
        } else {
            // 1.7 all fine
            lookVectors = new Vector3d[]{
                    MCMath.getVectorForRotation(TrigHandler.VANILLA_MATH, data.getRotationTracker().getPitch(), data.getRotationTracker().getYaw()),
                    MCMath.getVectorForRotation(TrigHandler.LEGACY_FAST_MATH, data.getRotationTracker().getPitch(), data.getRotationTracker().getYaw())
            };
        }

        return lookVectors;
    }

    @Getter
    private static class PiercingTestBox {
        private final BoundingBox smallBox;
        private final BoundingBox bigBox;
        private boolean missed = false;
        private final UUID uuid;

        public PiercingTestBox(BoundingBox smallBox, BoundingBox bigBox, UUID uuid) {
            this.smallBox = smallBox;
            this.bigBox = bigBox;
            this.uuid = uuid;
        }

        public void mightHaveMissed() {
            this.missed = true;
        }
    }
}
