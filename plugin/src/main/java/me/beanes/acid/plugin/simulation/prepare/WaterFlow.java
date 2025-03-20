package me.beanes.acid.plugin.simulation.prepare;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.MaterialType;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.simulation.data.MotionArea;
import me.beanes.acid.plugin.util.BlockFaces;
import me.beanes.acid.plugin.util.BlockUtil;
import me.beanes.acid.plugin.util.MCMath;
import me.beanes.acid.plugin.util.splitstate.SplitState;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

public class WaterFlow {
    public static MotionArea getFlowArea(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        MotionArea area = new MotionArea(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
        int flowDecay = getFlowDecay(state);

        for (BlockFace face : BlockFaces.HORIZONTAL_PLANE_FACES) {
            int newX = x + face.getModX();
            int newZ = z + face.getModZ();

            SplitState<WrappedBlockState> splitState = data.getWorldTracker().getBlock(newX, y, newZ);

            IntCollection possibleMultipliers = new IntArraySet();
            processBlock(data, flowDecay, splitState.getValue(), newX, y, newZ, possibleMultipliers);

            if (splitState.getOldValue() != null) {
                processBlock(data, flowDecay, splitState.getOldValue(), newX, y, newZ, possibleMultipliers);
            }

            boolean uncertain = possibleMultipliers.size() > 1;

            for (int multiplier : possibleMultipliers) {
                double addX = (newX - x) * multiplier;
                double addZ = (newZ - z) * multiplier;

                if (uncertain) {
                    area.extendX(addX);
                    area.extendZ(addZ);
                } else {
                    area.minX += addX;
                    area.maxX += addX;
                    area.minZ += addZ;
                    area.maxZ += addZ;
                }
            }
        }

        if (state.getLevel() >= 8) {
            for (BlockFace face : BlockFaces.HORIZONTAL_PLANE_FACES) {
                int newX = x + face.getModX();
                int newZ = z + face.getModZ();

                SplitState<WrappedBlockState> splitState = data.getWorldTracker().getBlock(newX, y, newZ);
                SplitState<WrappedBlockState> splitStateUp = data.getWorldTracker().getBlock(newX, y + 1, newZ);

                SplitStateBoolean mainSolid = isSolid(splitState, face);
                SplitStateBoolean upSolid = isSolid(splitStateUp, face);

                if (mainSolid.possible() || upSolid.possible()) {
                    area = normalise(area); // TODO: this needs to min max the noramlised area not override it

                    boolean certain = mainSolid == SplitStateBoolean.TRUE || upSolid == SplitStateBoolean.TRUE;

                    if (certain) {
                        area.minY -= 6.0D;
                        area.maxY -= 6.0D;
                    } else {
                        area.extendY(-6.0D);
                    }
                    break;
                }
            }
        }

        return normalise(area);
    }

    private static MotionArea normalise(MotionArea area) {
        double maximumX = Math.max(Math.abs(area.minX), Math.abs(area.maxX));
        double maximumY = Math.max(Math.abs(area.minY), Math.abs(area.maxY));
        double maximumZ = Math.max(Math.abs(area.minZ), Math.abs(area.maxZ));

        double maxSqrt = MCMath.sqrt_double(maximumX * maximumX + maximumY * maximumY + maximumZ * maximumZ);
        if (maxSqrt < 1.0E-4D) {
            return new MotionArea(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
        } else {
            return area.createNormalisedArea();
        }
    }

    private static void processBlock(PlayerData data, int flowDecay, WrappedBlockState otherState, int x, int y, int z, IntCollection possibleMultipliers) {
        int otherFlowDecay = getFlowDecay(otherState);

        if (otherFlowDecay < 0) { // No flow
            if (!BlockUtil.isSolid(otherState.getType().getMaterialType())) { // If the block is not a liquid, but and is not solid, we checkt he flow under
                SplitState<WrappedBlockState> splitState = data.getWorldTracker().getBlock(x, y - 1, z);

                otherFlowDecay = getFlowDecay(splitState.getValue());

                if (otherFlowDecay >= 0) {
                    possibleMultipliers.add(otherFlowDecay - (flowDecay - 8));
                }

                if (splitState.getOldValue() != null) {
                    int otherFlowDecayOld = getFlowDecay(splitState.getOldValue());

                    if (otherFlowDecayOld  >= 0) {
                        possibleMultipliers.add(otherFlowDecayOld - (flowDecay - 8));
                    }
                }
            }
        } else {
            possibleMultipliers.add(otherFlowDecay - flowDecay);
        }
    }

    private static boolean isBlockSolid(MaterialType material, BlockFace face) {
        return (face == BlockFace.UP || (material != MaterialType.ICE && BlockUtil.isSolid(material)));
    }

    private static SplitStateBoolean isSolid(SplitState<WrappedBlockState> splitState, BlockFace face) {
        boolean latestSolid = isBlockSolid(splitState.getValue().getType().getMaterialType(), face);
        boolean oldSolid = splitState.getOldValue() != null ? isBlockSolid(splitState.getOldValue().getType().getMaterialType(), face) : latestSolid;

        if (latestSolid == oldSolid) {
            return latestSolid ? SplitStateBoolean.TRUE : SplitStateBoolean.FALSE;
        } else {
            return SplitStateBoolean.POSSIBLE;
        }
    }

    private static int getFlowDecay(WrappedBlockState state) {
        if (state.getType() != StateTypes.WATER) {
            return -1;
        }

        int level = state.getLevel();
        return level >= 8 ? 0 : level;
    }

    public static float getLiquidHeightPercent(int level) {
        if (level >= 8) {
            level = 0;
        }

        return (float)(level + 1) / 9.0F;
    }
}
