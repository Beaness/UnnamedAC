package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3f;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.util.BlockFaces;
import me.beanes.acid.plugin.util.BoundingBox;

public class CocoaBlockProvider extends BlockProvider {
    @Override
    public BoundingBox getCollisionBoundingBox(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        BlockFace facing = state.getFacing();

        int age = state.getAge();
        int j = 4 + age * 2;
        int k = 5 + age * 2;
        float f = (float)j / 2.0F;

        switch (facing)
        {
            case SOUTH:
                return new BoundingBox(x + (double) ((8.0F - f) / 16.0F), y + (double) ((12.0F - (float)k) / 16.0F), z + (double) ((15.0F - (float)j) / 16.0F), x + (double) ((8.0F + f) / 16.0F), y + (double) 0.75F, z + (double) 0.9375F);
            case NORTH:
                return new BoundingBox(x + (double) ((8.0F - f) / 16.0F), y + (double) ((12.0F - (float)k) / 16.0F), z + (double) 0.0625F, x + (double) ((8.0F + f) / 16.0F), y + (double) 0.75F, z + (double) ((1.0F + (float)j) / 16.0F));
            case WEST:
                return new BoundingBox(x + (double) 0.0625F, y + (double) ((12.0F - (float)k) / 16.0F), z + (double) ((8.0F - f) / 16.0F), x + (double) ((1.0F + (float)j) / 16.0F), y + (double) 0.75F, z + (double) ((8.0F + f) / 16.0F));
            case EAST:
            default:
                return new BoundingBox(x + (double) ((15.0F - (float)j) / 16.0F), y + (double) ((12.0F - (float)k) / 16.0F), z + (double) ((8.0F - f) / 16.0F), x + (double) 0.9375F, y + (double) 0.75F, z + (double) ((8.0F + f) / 16.0F));
        }
    }

    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        BlockFace facing = state.getFacing();

        int age = state.getAge();
        int j = 4 + age * 2;
        int k = 5 + age * 2;
        float f = (float)j / 2.0F;

        switch (facing)
        {
            case SOUTH:
                return new BoundingBox((8.0F - f) / 16.0F, (12.0F - (float)k) / 16.0F, (15.0F - (float)j) / 16.0F,  (8.0F + f) / 16.0F, 0.75F, 0.9375F);
            case NORTH:
                return new BoundingBox((8.0F - f) / 16.0F, (12.0F - (float)k) / 16.0F, 0.0625F, (8.0F + f) / 16.0F, 0.75F, (1.0F + (float)j) / 16.0F);
            case WEST:
                return new BoundingBox(0.0625F, (12.0F - (float)k) / 16.0F, (8.0F - f) / 16.0F, (1.0F + (float)j) / 16.0F, 0.75F, (8.0F + f) / 16.0F);
            case EAST:
            default:
                return new BoundingBox((15.0F - (float)j) / 16.0F, (12.0F - (float)k) / 16.0F, (8.0F - f) / 16.0F, 0.9375F, 0.75F, (8.0F + f) / 16.0F);
        }
    }

    @Override
    public boolean isFullCube(WrappedBlockState state) {
        return false;
    }

    @Override
    public void onPlace(PlayerData data, int x, int y, int z, BlockFace face, Vector3f hit, int meta, WrappedBlockState state) {
        if (BlockFaces.isVertical(face)) {
            face = BlockFace.NORTH;
        }

        state.setFacing(face.getOppositeFace());
        state.setAge(0);
    }
}
