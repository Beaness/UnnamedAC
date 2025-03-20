package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.MaterialType;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.enums.Half;
import com.github.retrooper.packetevents.util.Vector3f;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.util.BlockFaces;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

public class TrapDoorBlockProvider extends BlockProvider {
    @Override
    public BoundingBox getCollisionBoundingBox(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        if (state.isOpen()) {
            BlockFace facing = state.getFacing();

            if (facing == BlockFace.NORTH) {
                return new BoundingBox(x, y, z + (double) 0.8125F, x + (double) 1.0F, y + (double) 1.0F, z + (double) 1.0F);
            }

            if (facing == BlockFace.SOUTH) {
                return new BoundingBox(x, y, z, x + (double) 1.0F, y + (double) 1.0F, z + (double) 0.1875F);
            }

            if (facing == BlockFace.WEST) {
                return new BoundingBox(x + (double) 0.8125F, y, z, x + (double) 1.0F, y + (double) 1.0F, z + (double) 1.0F);
            }

            if (facing == BlockFace.EAST) {
                return new BoundingBox(x, y, z, x + (double) 0.1875F, y + (double) 1.0F, z + (double) 1.0F);
            }
        } else {
            if (state.getHalf() == Half.TOP) {
                return new BoundingBox(x, y + (double) 0.8125F, z, x + (double) 1.0F, y + (double) 1.0F, z + (double) 1.0F);
            } else {
                return new BoundingBox(x, y, z, x + (double) 1.0F, y + (double) 0.1875F, z + (double) 1.0F);
            }
        }

        return new BoundingBox(x, y, z, x + 1.0F, y + 0.1875F, z + 1.0F);
    }

    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        if (state.isOpen()) {
            BlockFace facing = state.getFacing();

            if (facing == BlockFace.NORTH) {
                return new BoundingBox(0.0F, 0.0F, 0.8125F, 1.0F, 1.0F, 1.0F);
            }

            if (facing == BlockFace.SOUTH) {
                return new BoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.1875F);
            }

            if (facing == BlockFace.WEST) {
                return new BoundingBox(0.8125F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
            }

            if (facing == BlockFace.EAST) {
                return new BoundingBox(0.0F, 0.0F, 0.0F, 0.1875F, 1.0F, 1.0F);
            }
        } else {
            if (state.getHalf() == Half.TOP) {
                return new BoundingBox(0.0F, 0.8125F, 0.0F, 1.0F, 1.0F, 1.0F);
            } else {
                return new BoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 0.1875F, 1.0F);
            }
        }

        return new BoundingBox(x, y, z, x + 1.0F, y + 0.1875F, z + 1.0F);
    }

    @Override
    public boolean isFullCube(WrappedBlockState state) {
        return false;
    }

    @Override
    public SplitStateBoolean onActivate(PlayerData data, int x, int y, int z, WrappedBlockState state, boolean latest) {
        if (state.getType().getMaterialType() == MaterialType.METAL) {
            return SplitStateBoolean.TRUE;
        }

        state.setOpen(!state.isOpen());

        if (latest) {
            data.getWorldTracker().setBlock(x, y, z, state);
        } else {
            data.getWorldTracker().setOldBlock(x, y, z, state);
        }

        return SplitStateBoolean.TRUE;
    }

    @Override
    public void onPlace(PlayerData data, int x, int y, int z, BlockFace face, Vector3f hit, int meta, WrappedBlockState state) {
        if (BlockFaces.isHorizontal(face)) {
            state.setFacing(face);
            if (hit.getY() > 0.5F) {
                state.setHalf(Half.TOP);
            } else {
                state.setHalf(Half.BOTTOM);
            }
        }
    }
}
