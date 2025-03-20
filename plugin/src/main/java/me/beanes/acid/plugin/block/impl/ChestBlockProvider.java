package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.util.Vector3f;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

import java.util.List;

public class ChestBlockProvider extends BlockProvider {
    @Override
    public void addPossibleCollisionBoxes(PlayerData data, int x, int y, int z, WrappedBlockState state, BoundingBox mask, List<BoundingBox> list) {
        StateType type = state.getType();

        SplitStateBoolean north = data.getWorldTracker().isMaterial(x, y, z - 1, type);
        SplitStateBoolean south = data.getWorldTracker().isMaterial(x, y, z + 1, type);
        SplitStateBoolean west = data.getWorldTracker().isMaterial(x - 1, y, z, type);
        SplitStateBoolean east = data.getWorldTracker().isMaterial(x + 1, y, z, type);


        if (data.getUser().getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8)) {
            if (north == SplitStateBoolean.TRUE) {
                north = SplitStateBoolean.POSSIBLE;
            }

            if (south == SplitStateBoolean.TRUE) {
                south = SplitStateBoolean.POSSIBLE;
            }

            if (west == SplitStateBoolean.TRUE) {
                west = SplitStateBoolean.POSSIBLE;
            }

            if (east == SplitStateBoolean.TRUE) {
                east = SplitStateBoolean.POSSIBLE;
            }
        }

        if (north.possible()) {
            BoundingBox box = getNorthBox(x, y, z);
            if (mask.intersectsWith(box)) {
                list.add(box);
            }

            if (north == SplitStateBoolean.TRUE) {
                return;
            }
        }

        if (south.possible()) {
            BoundingBox box = getSouthBox(x, y, z);
            if (mask.intersectsWith(box)) {
                list.add(box);
            }

            if (south == SplitStateBoolean.TRUE) {
                return;
            }
        }

        if (west.possible()) {
            BoundingBox box = getWestBox(x, y, z);
            if (mask.intersectsWith(box)) {
                list.add(box);
            }

            if (west == SplitStateBoolean.TRUE) {
                return;
            }
        }

        if (east.possible()) {
            BoundingBox box = getEastBox(x, y, z);
            if (mask.intersectsWith(box)) {
                list.add(box);
            }

            if (east == SplitStateBoolean.TRUE) {
                return;
            }
        }

        BoundingBox box = getDefaultBox(x, y, z);

        if (mask.intersectsWith(box)) {
            list.add(box);
        }
    }

    @Override
    public SplitStateBoolean isColliding(PlayerData data, int x, int y, int z, WrappedBlockState state, BoundingBox mask) {
        StateType type = state.getType();

        SplitStateBoolean north = data.getWorldTracker().isMaterial(x, y, z - 1, type);
        SplitStateBoolean south = data.getWorldTracker().isMaterial(x, y, z + 1, type);
        SplitStateBoolean west = data.getWorldTracker().isMaterial(x - 1, y, z, type);
        SplitStateBoolean east = data.getWorldTracker().isMaterial(x + 1, y, z, type);

        if (north.possible()) {
            BoundingBox box = new BoundingBox(x + 0.0625F, y, z, x + 0.9375F, y + 0.875F, z + 0.9375F);
            if (mask.intersectsWith(box)) {
                return SplitStateBoolean.POSSIBLE;
            }
        }

        if (south.possible()) {
            BoundingBox box = new BoundingBox(x + 0.0625F, y, z + 0.0625F, x + 0.9375F, y + 0.875F, z + 1.0F);
            if (mask.intersectsWith(box)) {
                return SplitStateBoolean.POSSIBLE;
            }
        }

        if (west.possible()) {
            BoundingBox box = new BoundingBox(x, y, z + 0.0625F, x + 0.9375F, y + 0.875F, z + 0.9375F);
            if (mask.intersectsWith(box)) {
                return SplitStateBoolean.POSSIBLE;
            }
        }

        if (east.possible()) {
            BoundingBox box = new BoundingBox(x + 0.0625F, y, z + 0.0625F, x + 1.0F, y + 0.875F, z + 0.9375F);
            if (mask.intersectsWith(box)) {
                return SplitStateBoolean.POSSIBLE;
            }
        }

        BoundingBox box = getDefaultBox(x, y, z);

        if (mask.intersectsWith(box)) {
            return SplitStateBoolean.POSSIBLE;
        }

        return SplitStateBoolean.FALSE;
    }

    private BoundingBox getNorthBox(int x, int y, int z) {
        return new BoundingBox(x + (double) 0.0625F, y, z, x + (double)0.9375F, y + (double) 0.875F, z + (double) 0.9375F);
    }

    private BoundingBox getSouthBox(int x, int y, int z) {
        return new BoundingBox(x + (double) 0.0625F, y, z + (double) 0.0625F, x + (double) 0.9375F, y + (double) 0.875F, z + (double) 1.0F);
    }

    private BoundingBox getWestBox(int x, int y, int z) {
        return new BoundingBox(x, y, z + (double) 0.0625F, x + (double) 0.9375F, y + (double) 0.875F, z + (double) 0.9375F);
    }

    private BoundingBox getEastBox(int x, int y, int z) {
        return new BoundingBox(x + (double) 0.0625F, y, z + (double) 0.0625F, x + (double) 0.9375F, y + (double) 0.875F, z + (double) 0.9375F);
    }

    private BoundingBox getDefaultBox(int x, int y, int z) {
        return new BoundingBox(x + (double) 0.0625F, y, z + (double) 0.0625F, x + (double) 0.9375F, y + (double) 0.875F, z + (double) 0.9375F);
    }

    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        StateType type = state.getType();

        boolean north = data.getWorldTracker().isMaterial(x, y, z - 1, type) == SplitStateBoolean.TRUE;
        boolean south = data.getWorldTracker().isMaterial(x, y, z + 1, type) == SplitStateBoolean.TRUE;
        boolean west = data.getWorldTracker().isMaterial(x - 1, y, z, type) == SplitStateBoolean.TRUE;
        boolean east = data.getWorldTracker().isMaterial(x + 1, y, z, type) == SplitStateBoolean.TRUE;

        // Possible split state inaccuracy -> prioritize true states
        if (north) {
            return new BoundingBox(0.0625F, 0.0F, 0.0F, 0.9375F, 0.875F, 0.9375F);
        } else if (south) {
            return new BoundingBox(x + 0.0625F, y, z + 0.0625F, x + 0.9375F, y + 0.875F, z + 1.0F);
        } else if (west) {
            return new BoundingBox(0.0F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F);
        } else if (east) {
            return new BoundingBox(0.0625F, 0.0F, 0.0625F, 1.0F, 0.875F, 0.9375F);
        } else {
            return new BoundingBox(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F);
        }
    }

    @Override
    public boolean isFullCube(WrappedBlockState state) {
        return false;
    }

    @Override
    public void onPlace(PlayerData data, int x, int y, int z, BlockFace face, Vector3f hit, int meta, WrappedBlockState state) {
        state.setFacing(data.getRotationTracker().getHorizontalFacing().getOppositeFace());
    }

    @Override
    public SplitStateBoolean onActivate(PlayerData data, int x, int y, int z, WrappedBlockState state, boolean latest) {
        return SplitStateBoolean.TRUE;
    }
}
