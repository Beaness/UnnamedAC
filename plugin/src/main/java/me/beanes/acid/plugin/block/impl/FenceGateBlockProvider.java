package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3f;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.util.BlockFaces;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

public class FenceGateBlockProvider extends BlockProvider {
    @Override
    public BoundingBox getCollisionBoundingBox(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        if (state.isOpen()) {
            return null;
        } else {
            BlockFace facing = state.getFacing();

            if (facing == BlockFace.NORTH || facing == BlockFace.SOUTH) {
                return new BoundingBox(x, y, (float)z + 0.375F, x + 1, (float)y + 1.5F, (float)z + 0.625F);
            } else {
                return new BoundingBox((float)x + 0.375F, y, z, (float)x + 0.625F, (float)y + 1.5F, z + 1);
            }
        }
    }

    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        BlockFace facing = state.getFacing();

        if (facing == BlockFace.NORTH || facing == BlockFace.SOUTH) {
            return new BoundingBox(0.0F, 0.0F, 0.375F, 1.0F, 1.0F, 0.625F);
        } else {
            return new BoundingBox(0.375F, 0.0F, 0.0F, 0.625F, 1.0F, 1.0F);
        }
    }

    @Override
    public SplitStateBoolean onActivate(PlayerData data, int x, int y, int z, WrappedBlockState state, boolean latest) {
        if (!state.isOpen()) {
            BlockFace face = BlockFaces.fromAngle(data.getRotationTracker().getYaw());

            if (state.getFacing() == face.getOppositeFace()) {
                state.setFacing(face);
            }
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
        state.setFacing(data.getRotationTracker().getHorizontalFacing());
    }

    @Override
    public boolean isFullCube(WrappedBlockState state) {
        return false;
    }
}
