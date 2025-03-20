package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3f;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.util.BoundingBox;

public class WallSkullBlockProvider extends BlockProvider {
    @Override
    public BoundingBox getCollisionBoundingBox(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        switch (state.getFacing()) {
            case UP:
            default:
                return new BoundingBox(x + (double) 0.25F, y, z + (double) 0.25F, x + (double) 0.75F, y + (double) 0.5F, z + (double) 0.75F);
            case NORTH:
                return new BoundingBox(x + (double) 0.25F, y + (double) 0.25F, z + (double) 0.5F, x + (double) 0.75F, y + (double) 0.75F, z + (double) 1.0F);
            case SOUTH:
                return new BoundingBox(x + (double) 0.25F, y + (double) 0.25F, z, x + (double) 0.75F, y + (double) 0.75F, z + (double) 0.5F);
            case WEST:
                return new BoundingBox(x + (double) 0.5F, y + (double) 0.25F, z + (double) 0.25F, x + (double) 1.0F, y + (double) 0.75F, z + (double) 0.75F);
            case EAST:
                return new BoundingBox(x, y + (double) 0.25F, z + (double) 0.25F, x + (double) 0.5F, y + (double) 0.75F, z + (double) 0.75F);
        }
    }

    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        switch (state.getFacing()) {
            case UP:
            default:
                return new BoundingBox(0.25F, 0.0F, 0.25F, 0.75F, 0.5F, 0.75F);

            case NORTH:
                return new BoundingBox(0.25F, 0.25F, 0.5F, 0.75F, 0.75F, 1.0F);

            case SOUTH:
                return new BoundingBox(0.25F, 0.25F, 0.0F, 0.75F, 0.75F, 0.5F);

            case WEST:
                return new BoundingBox(0.5F, 0.25F, 0.25F, 1.0F, 0.75F, 0.75F);

            case EAST:
                return new BoundingBox(0.0F, 0.25F, 0.25F, 0.5F, 0.75F, 0.75F);
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
}
