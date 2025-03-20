package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3f;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.util.BlockFaces;
import me.beanes.acid.plugin.util.BoundingBox;

public class PistonBaseBlockProvider extends BlockProvider {
    @Override
    public BoundingBox getCollisionBoundingBox(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        if (state.isExtended()) {
            BlockFace facing = state.getFacing();

            switch (facing) {
                case DOWN:
                    return new BoundingBox(x, y + (double) 0.25F, z + (double) 0.0F, x + (double) 1.0F, y + (double) 1.0F, z + (double) 1.0F);
                case UP:
                    return new BoundingBox(x, y, z, x + (double) 1.0F, y + (double) 0.75F, z + (double) 1.0F);
                case NORTH:
                    return new BoundingBox(x, y, z + (double) 0.25F, x + (double) 1.0F, y + (double) 1.0F, z + (double) 1.0F);
                case SOUTH:
                    return new BoundingBox(x, y, z, x + (double) 1.0F,  y + (double) 1.0F, z + (double) 0.75F);
                case WEST:
                    return new BoundingBox(x + (double) 0.25F, y, z, x + (double) 1.0F, y + (double) 1.0F, z + (double) 1.0F);
                case EAST:
                    return new BoundingBox(x, y, z, x + (double) 0.75F, y + (double) 1.0F, z + (double) 1.0F);
                default:
                    return super.getCollisionBoundingBox(data, x, y, z, state);
            }
        } else {
            return super.getCollisionBoundingBox(data, x, y, z, state);
        }
    }

    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        if (state.isExtended()) {
            BlockFace facing = state.getFacing();

            switch (facing) {
                case DOWN:
                    return new BoundingBox(0.0F, 0.25F, 0.0F, 1.0F, 1.0F, 1.0F);
                case UP:
                    return new BoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 0.75F, 1.0F);
                case NORTH:
                    return new BoundingBox(0.0F, 0.0F, 0.25F, 1.0F, 1.0F, 1.0F);
                case SOUTH:
                    return new BoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.75F);
                case WEST:
                    return new BoundingBox(0.25F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
                case EAST:
                default:
                    return new BoundingBox(0.0F, 0.0F, 0.0F, 0.75F, 1.0F, 1.0F);
            }
        } else {
            return super.getCollisionBoundingBox(data, x, y, z, state);
        }
    }

    @Override
    public void onPlace(PlayerData data, int x, int y, int z, BlockFace face, Vector3f hit, int meta, WrappedBlockState state) {
        state.setFacing(BlockFaces.getFacing(data, x, y, z));
    }
}
