package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.enums.Face;
import com.github.retrooper.packetevents.util.Vector3f;
import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.util.BlockFaces;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

public class LeverBlockProvider extends BlockProvider {
    @Override
    public SplitStateBoolean onActivate(PlayerData data, int x, int y, int z, WrappedBlockState state, boolean latest) {
        return SplitStateBoolean.TRUE;
    }

    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        if (state.getFace() == Face.CEILING) {
            return new BoundingBox(0.5F - 0.25F, 0.0F, 0.5F - 0.25F, 0.5F + 0.25F, 0.6F, 0.5F + 0.25F);
        }

        if (state.getFace() == Face.FLOOR) {
            return new BoundingBox(0.5F - 0.25F, 0.0F, 0.5F - 0.25F, 0.5F + 0.25F, 0.6F, 0.5F + 0.25F);
        }

        BlockFace facing = state.getFacing();

        switch (facing) {
            case EAST:
                return new BoundingBox(0.0F, 0.2F, 0.5F - 0.1875F, 0.1875F * 2.0F, 0.8F, 0.5F + 0.1875F);

            case WEST:
                return new BoundingBox(1.0F - 0.1875F * 2.0F, 0.2F, 0.5F - 0.1875F, 1.0F, 0.8F, 0.5F + 0.1875F);

            case SOUTH:
                return new BoundingBox(0.5F - 0.1875F, 0.2F, 0.0F, 0.5F + 0.1875F, 0.8F, 0.1875F * 2.0F);

            case NORTH:
            default:
                return new BoundingBox(0.5F - 0.1875F, 0.2F, 1.0F - 0.1875F * 2.0F, 0.5F + 0.1875F, 0.8F, 1.0F);
        }
    }

    @Override
    public void onPlace(PlayerData data, int x, int y, int z, BlockFace hitFace, Vector3f hit, int meta, WrappedBlockState state) {
        BlockFace targetFace = correctBlockFace(hitFace, data.getRotationTracker().getHorizontalFacing());

        if (canStay(data, x, y, z, hitFace.getOppositeFace())) {
            state.setFace(transformBlockFace(hitFace));
            state.setFacing(targetFace);
            return;
        }

        for (BlockFace possible : BlockFaces.HORIZONTAL_PLANE_FACES) {
            if (possible != hitFace && this.canStay(data, x, y, z, hitFace.getOppositeFace())) {
                state.setFace(transformBlockFace(hitFace));
                state.setFacing(targetFace);
                return;
            }
        }

        if (canStay(data, x, y - 1, z, BlockFace.DOWN)) {
            state.setFace(Face.CEILING);
            state.setFacing(targetFace);
        }
    }

    private Face transformBlockFace(BlockFace face) {
        if (face == BlockFace.UP) {
            return Face.FLOOR;
        } else if (face == BlockFace.DOWN) {
            return Face.CEILING;
        } else {
            return Face.WALL;
        }
    }

    private BlockFace correctBlockFace(BlockFace face, BlockFace targetFace) {
        if (face == BlockFace.UP || face == BlockFace.DOWN) {
            if (targetFace == BlockFace.SOUTH) {
                return BlockFace.NORTH;
            } else if (targetFace == BlockFace.EAST) {
                return BlockFace.WEST;
            }
        }

        return targetFace;
    }

    private boolean canStay(PlayerData data, int x, int y, int z, BlockFace face) {
        WrappedBlockState targetState = data.getWorldTracker().getBlock(x + face.getModX(), y + face.getModY(), z + face.getModZ()).getValue();
        return face == BlockFace.DOWN ? Acid.get().getBlockManager().doesBlockHaveSolidTopSurface(targetState) : Acid.get().getBlockManager().isNormalCube(targetState);
    }
}
