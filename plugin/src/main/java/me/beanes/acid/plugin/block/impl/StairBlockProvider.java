package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.protocol.world.states.enums.Half;
import com.github.retrooper.packetevents.util.Vector3f;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.SplitState;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

import java.util.ArrayList;
import java.util.List;

public class StairBlockProvider extends BlockProvider {

    // TODO: refactor this to support split states AND raytrace some day... (fuck stairs)

    @Override
    public void addPossibleCollisionBoxes(PlayerData data, int x, int y, int z, WrappedBlockState state, BoundingBox mask, List<BoundingBox> list) {
        // Base collision
        BoundingBox base = state.getHalf() == Half.TOP ? new BoundingBox(x, y + 0.5F, z, x + 1.0F, y + 1.0F, z + 1.0F) : new BoundingBox(x, y, z, x + 1.0F, y + 0.5F, z + 1.0F);
        if (mask.intersectsWith(base)) {
            list.add(base);
        }

        if (checkConnections(data, x, y, z, state, mask, list)) {
            checkConnections2(data, x, y, z, state, mask, list);
        }
    }

    @Override
    public SplitStateBoolean isColliding(PlayerData data, int x, int y, int z, WrappedBlockState state, BoundingBox mask) {
        // Cba with this one for now
        List<BoundingBox> boxes = new ArrayList<>();
        addPossibleCollisionBoxes(data, x, y, z, state, mask, boxes);
        if (!boxes.isEmpty()) {
            return SplitStateBoolean.POSSIBLE;
        }

        return SplitStateBoolean.FALSE;
    }

    @Override
    public boolean isFullCube(WrappedBlockState state) {
        return false;
    }

    private boolean checkConnections(PlayerData data, int x, int y, int z, WrappedBlockState state, BoundingBox mask, List<BoundingBox> list) {
        BlockFace facing = state.getFacing();
        Half half = state.getHalf();

        float minY = 0.5F;
        float maxY = 1.0F;

        if (half == Half.TOP) {
            minY = 0.0F;
            maxY = 0.5F;
        }

        float minX = 0.0F;
        float maxX = 1.0F;
        float minZ = 0.0F;
        float maxZ = 0.5F;
        boolean isConnected = true;

        if (facing == BlockFace.EAST) {
            minX = 0.5F;
            maxZ = 1.0F;

            SplitState<WrappedBlockState> splitState = data.getWorldTracker().getBlock(x + 1, y, z);

            if (isBlockStairs(splitState.getValue()) && half == splitState.getValue().getHalf()) {
                BlockFace otherFacing = splitState.getValue().getFacing();

                if (otherFacing == BlockFace.NORTH && !isSameStair(state, data.getWorldTracker().getBlock(x, y, z + 1).getValue())) {
                    maxZ = 0.5F;
                    isConnected = false;
                } else if (otherFacing == BlockFace.SOUTH && !isSameStair(state, data.getWorldTracker().getBlock(x, y, z - 1).getValue())) {
                    minZ = 0.5F;
                    isConnected = false;
                }
            }
        } else if (facing == BlockFace.WEST) {
            maxX = 0.5F;
            maxZ = 1.0F;

            SplitState<WrappedBlockState> splitState = data.getWorldTracker().getBlock(x - 1, y, z);

            if (isBlockStairs(splitState.getValue()) && half == splitState.getValue().getHalf()) {
                BlockFace otherFacing = splitState.getValue().getFacing();

                if (otherFacing == BlockFace.NORTH && !isSameStair(state, data.getWorldTracker().getBlock(x, y, z + 1).getValue())) {
                    maxZ = 0.5F;
                    isConnected = false;
                } else if (otherFacing == BlockFace.SOUTH && !isSameStair(state, data.getWorldTracker().getBlock(x, y, z - 1).getValue())) {
                    minZ = 0.5F;
                    isConnected = false;
                }
            }
        } else if (facing == BlockFace.SOUTH) {
            minZ = 0.5F;
            maxZ = 1.0F;

            SplitState<WrappedBlockState> splitState = data.getWorldTracker().getBlock(x, y, z + 1);

            if (isBlockStairs(splitState.getValue()) && half == splitState.getValue().getHalf()) {
                BlockFace otherFacing = splitState.getValue().getFacing();

                if (otherFacing == BlockFace.WEST && !isSameStair(state, data.getWorldTracker().getBlock(x + 1, y, z).getValue())) {
                    maxX = 0.5F;
                    isConnected = false;
                } else if (otherFacing == BlockFace.EAST && !isSameStair(state, data.getWorldTracker().getBlock(x - 1, y, z).getValue())) {
                    minX = 0.5F;
                    isConnected = false;
                }
            }
        } else if (facing == BlockFace.NORTH) {
            SplitState<WrappedBlockState> splitState = data.getWorldTracker().getBlock(x, y, z - 1);

            if (isBlockStairs(splitState.getValue()) && half == splitState.getValue().getHalf()) {
                BlockFace otherFacing = splitState.getValue().getFacing();

                if (otherFacing == BlockFace.WEST && !isSameStair(state, data.getWorldTracker().getBlock(x + 1, y, z).getValue())) {
                    maxX = 0.5F;
                    isConnected = false;
                } else if (otherFacing == BlockFace.EAST && !isSameStair(state, data.getWorldTracker().getBlock(x - 1, y, z).getValue())) {
                    minX = 0.5F;
                    isConnected = false;
                }
            }
        }

        BoundingBox box = new BoundingBox(x + minX, y + minY, z + minZ, x + maxX, y + maxY, z + maxZ);

        if (mask.intersectsWith(box)) {
            list.add(box);
        }

        return isConnected;
    }

    private void checkConnections2(PlayerData data, int x, int y, int z, WrappedBlockState state, BoundingBox mask, List<BoundingBox> list) {
        BlockFace facing = state.getFacing();
        Half half = state.getHalf();

        float minY = 0.5F;
        float maxY = 1.0F;

        if (half == Half.TOP) {
            minY = 0.0F;
            maxY = 0.5F;
        }

        float minX = 0.0F;
        float maxX = 0.5F;
        float minZ = 0.5F;
        float maxZ = 1.0F;
        boolean isConnected = false;

        if (facing == BlockFace.EAST) {
            SplitState<WrappedBlockState> splitState = data.getWorldTracker().getBlock(x + 1, y, z);

            if (isBlockStairs(splitState.getValue()) && half == splitState.getValue().getHalf()) {
                BlockFace otherFacing = splitState.getValue().getFacing();

                if (otherFacing == BlockFace.NORTH && !isSameStair(state, data.getWorldTracker().getBlock(x, y, z - 1).getValue())) {
                    minZ = 0.0F;
                    maxZ = 0.5F;
                    isConnected = true;
                } else if (otherFacing == BlockFace.SOUTH && !isSameStair(state, data.getWorldTracker().getBlock(x, y, z + 1).getValue())) {
                    minZ = 0.5F;
                    maxZ = 1.0F;
                    isConnected = true;
                }
            }
        } else if (facing == BlockFace.WEST) {
            SplitState<WrappedBlockState> splitState = data.getWorldTracker().getBlock(x + 1, y, z);

            if (isBlockStairs(splitState.getValue()) && half == splitState.getValue().getHalf()) {
                minX = 0.5F;
                maxX = 1.0F;

                BlockFace otherFacing = splitState.getValue().getFacing();

                if (otherFacing == BlockFace.NORTH && !isSameStair(state, data.getWorldTracker().getBlock(x, y, z - 1).getValue())) {
                    minZ = 0.0F;
                    maxZ = 0.5F;
                    isConnected = true;
                } else if (otherFacing == BlockFace.SOUTH && !isSameStair(state, data.getWorldTracker().getBlock(x, y, z + 1).getValue())) {
                    minZ = 0.5F;
                    maxZ = 1.0F;
                    isConnected = true;
                }
            }
        } else if (facing == BlockFace.SOUTH) {
            SplitState<WrappedBlockState> splitState = data.getWorldTracker().getBlock(x, y, z - 1);

            if (isBlockStairs(splitState.getValue()) && half == splitState.getValue().getHalf()) {
                minZ = 0.0F;
                maxZ = 0.5F;
                BlockFace otherFacing = splitState.getValue().getFacing();

                if (otherFacing == BlockFace.WEST && !isSameStair(state, data.getWorldTracker().getBlock(x - 1, y, z).getValue())) {
                    isConnected = true;
                } else if (otherFacing == BlockFace.EAST && !isSameStair(state, data.getWorldTracker().getBlock(x + 1, y, z).getValue())) {
                    minX = 0.5F;
                    maxX = 1.0F;
                    isConnected = true;
                }
            }
        } else if (facing == BlockFace.NORTH) {
            SplitState<WrappedBlockState> splitState = data.getWorldTracker().getBlock(x, y, z + 1);

            if (isBlockStairs(splitState.getValue()) && half == splitState.getValue().getHalf()) {
                BlockFace otherFacing = splitState.getValue().getFacing();

                if (otherFacing == BlockFace.WEST && !isSameStair(state, data.getWorldTracker().getBlock(x - 1, y, z).getValue())) {
                    isConnected = true;
                } else if (otherFacing == BlockFace.EAST && !isSameStair(state, data.getWorldTracker().getBlock(x + 1, y, z).getValue())) {
                    minX = 0.5F;
                    maxX = 1.0F;
                    isConnected = true;
                }
            }
        }

        if (isConnected) {
            BoundingBox box = new BoundingBox(x + minX, y + minY, z + minZ, x + maxX, y + maxY, z + maxZ);

            if (mask.intersectsWith(box)) {
                list.add(box);
            }
        }
    }

    private static boolean isBlockStairs(WrappedBlockState state) {
        return BlockTags.STAIRS.contains(state.getType());
    }

    private static boolean isSameStair(WrappedBlockState stair, WrappedBlockState other) {
        return isBlockStairs(other) && stair.getHalf() == other.getHalf() && stair.getFacing() == other.getFacing();
    }

    @Override
    public void onPlace(PlayerData data, int x, int y, int z, BlockFace face, Vector3f hit, int meta, WrappedBlockState state) {
        state.setFacing(data.getRotationTracker().getHorizontalFacing());

        if (face != BlockFace.DOWN && (face == BlockFace.UP || (double)hit.getY() <= 0.5D)) {
            state.setHalf(Half.BOTTOM);
        } else {
            state.setHalf(Half.TOP);
        }
    }
}
