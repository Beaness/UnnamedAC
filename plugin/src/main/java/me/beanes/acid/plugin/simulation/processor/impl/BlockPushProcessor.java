package me.beanes.acid.plugin.simulation.processor.impl;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.simulation.data.MotionArea;
import me.beanes.acid.plugin.simulation.processor.AreaProcessor;
import me.beanes.acid.plugin.util.BoxUtil;
import me.beanes.acid.plugin.util.MCMath;
import me.beanes.acid.plugin.util.splitstate.SplitState;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

public class BlockPushProcessor extends AreaProcessor {

    public BlockPushProcessor(PlayerData data) {
        super(data);
    }

    public void processArea(MotionArea area) {
        double x = data.getPositionTracker().getLastX();
        double y = data.getPositionTracker().getLastY();
        double z = data.getPositionTracker().getLastZ();

        this.pushOutOfBlocks(x - (double) BoxUtil.PLAYER_WIDTH * 0.35D, y + 0.5D, z + (double) BoxUtil.PLAYER_WIDTH * 0.35D, area);
        this.pushOutOfBlocks(x - (double) BoxUtil.PLAYER_WIDTH * 0.35D, y + 0.5D, z - (double) BoxUtil.PLAYER_WIDTH * 0.35D, area);
        this.pushOutOfBlocks(x + (double) BoxUtil.PLAYER_WIDTH * 0.35D, y + 0.5D, z - (double) BoxUtil.PLAYER_WIDTH * 0.35D, area);
        this.pushOutOfBlocks(x + (double) BoxUtil.PLAYER_WIDTH * 0.35D, y + 0.5D, z + (double) BoxUtil.PLAYER_WIDTH * 0.35D, area);
    }

    private static final int WEST = 0b0001;
    private static final int EAST = 0b0010;
    private static final int NORTH = 0b0100;
    private static final int SOUTH = 0b1000;

    private void pushOutOfBlocks(double checkX, double checkY, double checkZ, MotionArea area) {
        int x = MCMath.floor_double(checkX);
        int y = MCMath.floor_double(checkY);
        int z = MCMath.floor_double(checkZ);

        double offsetX = checkX - (double) x;
        double offsetZ = checkZ - (double) z;
        SplitStateBoolean mainOpen = this.isOpenBlockSpace(x, y, z);

        if (mainOpen.notPossible()) {
            double bestOffsetCertain = 9999.0D;
            double bestOffsetUncertain = 9999.0D;

            SplitStateBoolean west = this.isOpenBlockSpace(x - 1, y, z);
            SplitStateBoolean east = this.isOpenBlockSpace(x + 1, y, z);
            SplitStateBoolean north = this.isOpenBlockSpace(x, y, z - 1);
            SplitStateBoolean south = this.isOpenBlockSpace(x, y, z + 1);

            int doMask = 0; // West | East | Nort | South

            if (west.possible()) {
                // If west is possible, let's make it a possible motion offset
                doMask |= WEST;

                // If west is certain, we can update the bestOffset
                if (west == SplitStateBoolean.TRUE) {
                    bestOffsetCertain = offsetX;
                }

                bestOffsetUncertain = Math.min(bestOffsetUncertain, offsetX);
            }


            if (east.possible()) {
                double offset = 1.0D - offsetX;

                if (offset < bestOffsetCertain) {
                    // If east is possible, let's make it a possible motion offset
                    doMask |= EAST;

                    if (east == SplitStateBoolean.TRUE && offset < bestOffsetUncertain) {
                        // We are certain about east
                        doMask &= ~WEST;
                        bestOffsetCertain = offset;
                    }

                    bestOffsetUncertain = Math.min(bestOffsetUncertain, offset);
                }
            }

            if (north.possible()) {
                if (offsetZ < bestOffsetCertain) {
                    doMask |= NORTH;

                    if (north == SplitStateBoolean.TRUE & offsetZ < bestOffsetUncertain) {
                        // We are certain about north
                        doMask &= ~WEST;
                        doMask &= ~EAST;
                        bestOffsetCertain = offsetZ;
                    }

                    bestOffsetUncertain = Math.min(bestOffsetUncertain, offsetX);
                }
            }

            if (south.possible()) {
                double offset = 1.0D - offsetZ;

                if (offset < bestOffsetCertain) {
                    doMask |= SOUTH;

                    if (south == SplitStateBoolean.TRUE && offset < bestOffsetUncertain) {
                        // We are certain about south
                        doMask &= ~WEST;
                        doMask &= ~EAST;
                        doMask &= ~NORTH;
                    }
                }
            }

            boolean uncertain = mainOpen.possible() || Integer.bitCount(doMask) > 1 || data.getPositionTracker().isLastUncertain();

            if ((doMask & WEST) != 0) {
                if (uncertain || west == SplitStateBoolean.POSSIBLE) {
                    area.allowX(-0.1F);
                } else {
                    area.minX = -0.1F;
                    area.maxX = -0.1F;
                }
            }

            if ((doMask & EAST) != 0) {
                if (uncertain || east == SplitStateBoolean.POSSIBLE) {
                    area.allowX(0.1F);
                } else {
                    area.minX = 0.1F;
                    area.maxX = 0.1F;
                }
            }

            if ((doMask & NORTH) != 0) {
                if (uncertain || north == SplitStateBoolean.POSSIBLE) {
                    area.allowZ(-0.1F);
                } else {
                    area.minZ = -0.1F;
                    area.maxZ = -0.1F;
                }
            }

            if ((doMask & SOUTH) != 0) {
                if (uncertain || south == SplitStateBoolean.POSSIBLE) {
                    area.allowZ(0.1F);
                } else {
                    area.minZ = 0.1F;
                    area.maxZ = 0.1F;
                }
            }
        }
    }

    private SplitStateBoolean isOpenBlockSpace(int x, int y, int z) {
        if (data.getPositionTracker().isLastUncertain()) {
            int minX, minY, minZ, maxX, maxY, maxZ;

            minX = MCMath.floor_double(data.getPositionTracker().getLastReportedX() - 0.03D);
            minY = MCMath.floor_double(data.getPositionTracker().getLastReportedY() - 0.03D);
            minZ = MCMath.floor_double(data.getPositionTracker().getLastReportedZ() - 0.03D);
            maxX = MCMath.floor_double(data.getPositionTracker().getLastReportedX() + 0.03D) + 1;
            maxY = MCMath.floor_double(data.getPositionTracker().getLastReportedY() + 1 + 0.03D) + 1;
            maxZ = MCMath.floor_double(data.getPositionTracker().getLastReportedZ() + 0.03D) + 1;

            SplitStateBoolean result = null;

            for (int itX = minX; itX <= maxX; ++itX) {
                for (int itY = minY; itY <= maxY; ++itY) {
                    for (int itZ = minZ; itZ <= maxZ; ++itZ) {
                        SplitState<WrappedBlockState> splitState = data.getWorldTracker().getBlock(x, y, z);

                        SplitStateBoolean open = isOpenBlock(splitState);

                        if (result != null) {
                            result = SplitStateBoolean.result(result, open);
                        } else {
                            result = open;
                        }

                        if (result == SplitStateBoolean.POSSIBLE) {
                            return result;
                        }
                    }
                }
            }

            return result;
        } else {
            SplitState<WrappedBlockState> splitState = data.getWorldTracker().getBlock(x, y, z);
            SplitState<WrappedBlockState> splitStateUp = data.getWorldTracker().getBlock(x, y + 1, z);

            return SplitStateBoolean.result(isOpenBlock(splitState), isOpenBlock(splitStateUp));
        }
    }

    private SplitStateBoolean isOpenBlock(SplitState<WrappedBlockState> state) {
        boolean latest = !Acid.get().getBlockManager().isNormalCube(state.getValue());
        boolean old = state.getOldValue() != null ? !Acid.get().getBlockManager().isNormalCube(state.getOldValue()) : latest;

        return SplitStateBoolean.result(latest, old);
    }
}
