package me.beanes.acid.plugin.simulation.processor.impl;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.simulation.data.MotionArea;
import me.beanes.acid.plugin.simulation.data.MotionAreaXZ;
import me.beanes.acid.plugin.simulation.processor.AreaProcessor;
import me.beanes.acid.plugin.util.BoxUtil;
import me.beanes.acid.plugin.util.MCMath;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.SplitState;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

public class SneakProcessor extends AreaProcessor {

    public SneakProcessor(PlayerData data) {
        super(data);
    }

    private static final double STEP = 0.05D;
    private boolean reduceX, reduceZ;

    public void prepareSneakReduce() {
        boolean canSneakStep = data.getPositionTracker().isLastOnGround() && data.getActionTracker().isSneaking();

        System.out.println("| canSneakStep=" + canSneakStep);

        if (!canSneakStep) {
            reduceX = false;
            reduceZ = false;
            return;
        }

        reduceX = checkEmpty(STEP, 0).possible() || checkEmpty(-STEP, 0).possible();
        reduceZ = checkEmpty(0, STEP).possible() || checkEmpty(0, -STEP).possible();

        System.out.println("reduceX=" + reduceX + " reduceZ=" + reduceZ);

        if (!reduceX || !reduceZ) {
            // Check XZ if  reduce X or reduce Z failed
            if (checkEmpty(STEP, STEP).possible() || checkEmpty(-STEP, STEP).possible() || checkEmpty(STEP, -STEP).possible() || checkEmpty(-STEP, -STEP).possible()) {
                reduceX = true;
                reduceZ = true;
            }
        }
    }


    // Sneaking is fundamentally flawed with how I made it in multiple ways
    // The biggest issue is that the bounding box fetching is "greedy" it sends as much as possible
    // The problem is we need the least amount of possible bounding boxes fetched
    public MotionAreaXZ process(MotionArea area) {
        if (!reduceX && !reduceZ) {
            return null;
        }

        MotionAreaXZ hiddenMotion = new MotionAreaXZ(area.minX, area.minZ, area.maxX, area.maxZ);

        if (reduceX) {
            area.allowX(0);
        }

        if (reduceZ) {
            area.allowZ(0);
        }


        if (reduceX || reduceZ) {
            return hiddenMotion;
        }

        return null;
    }

    private SplitStateBoolean checkEmpty(double stepX, double stepZ) {
        if (data.getPositionTracker().isUncertain()) {
            BoundingBox bigger, smaller;

            bigger = BoxUtil.getPlayerBox(data.getPositionTracker().getReportedX() + stepX, data.getPositionTracker().getLastReportedY() - 1.0D, data.getPositionTracker().getReportedZ() + stepZ, 0.03D);
            smaller = BoxUtil.getPlayerBox(data.getPositionTracker().getReportedX() + stepX, data.getPositionTracker().getLastReportedY() - 1.0D, data.getPositionTracker().getReportedZ() + stepZ, -0.03D);

            System.out.println("| Uncertain Empty Check: " + bigger + " " + smaller);

            System.out.println("| BOX BIGGER: " + bigger);
            System.out.println("| BOX SMALLER: " + smaller);

            return SplitStateBoolean.result(isEmpty(bigger), isEmpty(smaller));
        } else {
            BoundingBox box = BoxUtil.getPlayerBox(data.getPositionTracker().getX() + stepX, data.getPositionTracker().getLastY() - 1.0D, data.getPositionTracker().getZ() + stepZ, 0D);

            boolean empty = isEmpty(box);

            System.out.println("| Certain Empty: " + empty);

            System.out.println("| BOX: " + box);

            return empty ? SplitStateBoolean.TRUE : SplitStateBoolean.FALSE;
        }
    }

    // Only when 100% certain the box is empty this function will 
    public boolean isEmpty(BoundingBox mask) {
        int minX = MCMath.floor_double(mask.getMinX());
        int minY = MCMath.floor_double(mask.getMinY());
        int minZ = MCMath.floor_double(mask.getMinZ());
        int maxX = MCMath.floor_double(mask.getMaxX());
        int maxY = MCMath.floor_double(mask.getMaxY());
        int maxZ = MCMath.floor_double(mask.getMaxZ());

        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                for (int y = minY; y <= maxY; ++y) {
                    SplitState<WrappedBlockState> splitState = data.getWorldTracker().getBlock(x, y, z);
                    WrappedBlockState latest = splitState.getValue();
                    WrappedBlockState old = splitState.getOldValue();

                    if (Acid.get().getBlockManager().isColliding(data, x, y, z, latest, mask) == SplitStateBoolean.TRUE) {
                        return false;
                    }

                    if (old != null) {
                        if (Acid.get().getBlockManager().isColliding(data, x, y, z, old, mask) == SplitStateBoolean.TRUE) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

}
