package me.beanes.acid.plugin.simulation.processor.impl;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.simulation.data.CollisionResult;
import me.beanes.acid.plugin.simulation.data.MotionArea;
import me.beanes.acid.plugin.simulation.processor.AreaProcessor;
import me.beanes.acid.plugin.util.BoxUtil;
import me.beanes.acid.plugin.util.MCMath;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.SplitState;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

import java.util.ArrayList;
import java.util.List;

public class CollisionProcessor extends AreaProcessor {
    public CollisionProcessor(PlayerData data) {
        super(data);
    }

    private static final float STEP_HEIGHT = 0.6F;

    public CollisionResult process(MotionArea area) {
        SplitStateBoolean collidedX = SplitStateBoolean.FALSE;
        SplitStateBoolean collidedY = SplitStateBoolean.FALSE;
        SplitStateBoolean collidedZ = SplitStateBoolean.FALSE;

        // https://imgur.com/a/bB2zwE8 if the purple is what the server knows (because of 0.03) and the yellow is what the client actually thinks
        // we'll never get the orange arrow (due to collision) because of where the server thinks the player box is
        // we can fix this though by creating a smaller box that reduces by 0.03 and then adding the motion offsets + 0.03D to it!
        // You get this then: https://imgur.com/a/1G5s4Jd (not up to scale I made this very quickly and my explanation probably sucks)
        // The black box is the reduced box and the black gray arrow is then the original arrow + 0.03!
        // This should account for 0.03 uncertainty in this collision system
        boolean uncertain = data.getPositionTracker().isLastUncertain();

        BoundingBox playerBox;

        if (uncertain) {
            playerBox = BoxUtil.getPlayerBox(data.getPositionTracker().getLastReportedX(), data.getPositionTracker().getLastReportedY(), data.getPositionTracker().getLastReportedZ(), -0.03D);
        } else {
            playerBox = data.getPositionTracker().getLastBox();
        }

        BoundingBox collisionBox = getCollisionBox(area);

        List<BoundingBox> possibleCollisions = getAllCollidingBoxes(collisionBox);

        double maxX = uncertain ? area.maxX + 0.03D : area.maxX;
        double maxY = uncertain ? area.maxY + 0.03D : area.maxY;
        double maxZ = uncertain ? area.maxZ + 0.03D : area.maxZ;
        double minX = uncertain ? area.minX - 0.03D : area.minX;
        double minY = uncertain ? area.minY - 0.03D : area.minY;
        double minZ = uncertain ? area.minZ - 0.03D : area.minZ;

        for (BoundingBox box : possibleCollisions) {
            if (area.maxY > 0) {
                double newMaxY = box.offsetY(playerBox, maxY);
                if (maxY != newMaxY) {
                    area.allowY(newMaxY - (uncertain ? 0.03D : 0.0D));
                    collidedY = SplitStateBoolean.POSSIBLE;
                }
            }

            if (area.maxX > 0) {
                double newMaxX = box.offsetX(playerBox, maxX);
                if (maxX != newMaxX) {
                    area.allowX(newMaxX - (uncertain ? 0.03D : 0.0D));
                    collidedX = SplitStateBoolean.POSSIBLE;
                }
            }

            if (area.maxZ > 0) {
                double newMaxZ = box.offsetZ(playerBox, maxZ);
                if (maxZ != newMaxZ) {
                    area.allowZ(newMaxZ - (uncertain ? 0.03D : 0.0D));
                    collidedZ = SplitStateBoolean.POSSIBLE;
                }
            }

            if (area.minY < 0) {
                double newMinY = box.offsetY(playerBox, minY);
                if (minY != newMinY) {
                    area.allowY(newMinY + (uncertain ? 0.03D : 0.0D));
                    collidedY = SplitStateBoolean.POSSIBLE;
                }
            }

            if (area.minX < 0) {
                double newMinX = box.offsetX(playerBox, minX);
                if (minX != newMinX) {
                    area.allowX(newMinX + (uncertain ? 0.03D : 0.0D));
                    collidedX = SplitStateBoolean.POSSIBLE;
                }
            }

            if (area.minZ < 0) {
                double newMinZ = box.offsetZ(playerBox, minZ);
                if (minZ != newMinZ) {
                    area.allowZ(newMinZ + (uncertain ? 0.03D : 0.0D));
                    collidedZ = SplitStateBoolean.POSSIBLE;
                }
            }
        }

        boolean canStep = data.getPositionTracker().isLastOnGround() || collidedY.possible() && area.minY < 0.0D;
        boolean possibleCollidedHorizontally = collidedX.possible() || collidedZ.possible();


        if (canStep && possibleCollidedHorizontally) {
            // TODO: The step system might be a bit too lenient :(
            double stepSize = STEP_HEIGHT + (uncertain ? 0.03D : 0.0D);

            BoundingBox stepBox = playerBox.offset(0, STEP_HEIGHT, 0);
            List<BoundingBox> possibleStepCollisions = getAllCollidingBoxes(getStepCollisionBox(area));

            for (BoundingBox box : possibleStepCollisions) {
                // The first step stuff
                double stepY = box.offsetY(stepBox, -stepSize);

                double diff = stepSize + stepY;

                if (diff > 0.0D) {
                    collidedY = SplitStateBoolean.POSSIBLE;
                    area.allowY(diff);
                }

                double stepHackery = box.offsetY(playerBox, stepSize);
                if (stepHackery != stepSize) {
                    collidedY = SplitStateBoolean.POSSIBLE;
                    area.allowY(-stepHackery); // this used to be -something

                    System.out.println("Some StepY=" + stepHackery);
                }

                if (area.maxX > 0) {
                    double newMaxX = box.offsetX(playerBox, maxX);
                    if (maxX != newMaxX) {
                        area.allowX(newMaxX - (uncertain ? 0.03D : 0.0D));
                        collidedX = SplitStateBoolean.POSSIBLE;
                    }
                }

                if (area.maxZ > 0) {
                    double newMaxZ = box.offsetZ(playerBox, maxZ);
                    if (maxZ != newMaxZ) {
                        area.allowZ(newMaxZ - (uncertain ? 0.03D : 0.0D));
                        collidedZ = SplitStateBoolean.POSSIBLE;
                    }
                }

                if (area.minY < 0) {
                    double newMinY = box.offsetY(playerBox, minY);
                    if (minY != newMinY) {
                        area.allowY(newMinY + (uncertain ? 0.03D : 0.0D));
                        collidedY = SplitStateBoolean.POSSIBLE;
                    }
                }

                if (area.minX < 0) {
                    double newMinX = box.offsetX(playerBox, minX);
                    if (minX != newMinX) {
                        area.allowX(newMinX + (uncertain ? 0.03D : 0.0D));
                        collidedX = SplitStateBoolean.POSSIBLE;
                    }
                }

                if (area.minZ < 0) {
                    double newMinZ = box.offsetZ(playerBox, minZ);
                    if (minZ != newMinZ) {
                        area.allowZ(newMinZ + (uncertain ? 0.03D : 0.0D));
                        collidedZ = SplitStateBoolean.POSSIBLE;
                    }
                }
            }

            // More step hackery!!
            if (collidedY.possible()) {
                area.allowY(0);
            }
        }

        return new CollisionResult(collidedX, collidedY, collidedZ, area.minY, area.maxY);
    }

    private BoundingBox getCollisionBox(MotionArea area) {
        BoundingBox playerBox = data.getPositionTracker().getLastBox();

        double minX = area.minX < 0 ? playerBox.getMinX() + area.minX : playerBox.getMinX();
        double minY = area.minY < 0 ? playerBox.getMinY() + area.minY : playerBox.getMinY();
        double minZ = area.minZ < 0 ? playerBox.getMinZ() + area.minZ : playerBox.getMinZ();
        double maxX = area.maxX > 0 ? playerBox.getMaxX() + area.maxX : playerBox.getMaxX();
        double maxY = area.maxY > 0 ? playerBox.getMaxY() + area.maxY : playerBox.getMaxY();
        double maxZ = area.maxZ > 0 ? playerBox.getMaxZ() + area.maxZ : playerBox.getMaxZ();

        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private BoundingBox getStepCollisionBox(MotionArea area) {
        BoundingBox playerBox = data.getPositionTracker().getLastBox();

        double minX = area.minX < 0 ? playerBox.getMinX() + area.minX : playerBox.getMinX();
        double minZ = area.minZ < 0 ? playerBox.getMinZ() + area.minZ : playerBox.getMinZ();
        double maxX = area.maxX > 0 ? playerBox.getMaxX() + area.maxX : playerBox.getMaxX();
        double maxZ = area.maxZ > 0 ? playerBox.getMaxZ() + area.maxZ : playerBox.getMaxZ();

        return new BoundingBox(minX, playerBox.getMinY(), minZ, maxX, playerBox.getMaxY() + (double) STEP_HEIGHT, maxZ);
    }

    // This method returns both old possible state and new state for collision
    public List<BoundingBox> getAllCollidingBoxes(BoundingBox bb) {
        List<BoundingBox> list = new ArrayList<>();
        int minX = MCMath.floor_double(bb.getMinX());
        int maxX = MCMath.floor_double(bb.getMaxX() + 1.0D);
        int minY = MCMath.floor_double(bb.getMinY());
        int maxY = MCMath.floor_double(bb.getMaxY() + 1.0D);
        int minZ = MCMath.floor_double(bb.getMinZ());
        int maxZ = MCMath.floor_double(bb.getMaxZ() + 1.0D);


        for (int x = minX; x < maxX; ++x) {
            for (int z = minZ; z < maxZ; ++z) {
                for (int y = minY - 1; y < maxY; ++y) {
                    // TODO: border collisions here or something idk might not have read it correctly but hey border collisions have to be done!
                    SplitState<WrappedBlockState> splitState = data.getWorldTracker().getBlock(x, y, z);

                    if (!splitState.getValue().getType().isAir()) {
                        Acid.get().getBlockManager().addPossibleCollisionBoxes(data, x, y, z, splitState.getValue(), bb, list);
                    }

                    if (splitState.getOldValue() != null && !splitState.getOldValue().getType().isAir()) {
                        Acid.get().getBlockManager().addPossibleCollisionBoxes(data, x, y, z, splitState.getOldValue(), bb, list);
                    }
                }
            }
        }

        // TODO: boat collisions -> very painful because we need to track boats...

        return list;
    }
}
