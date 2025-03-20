package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3f;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.util.BlockFaces;
import me.beanes.acid.plugin.util.BoundingBox;

public class TripWireHookBlockProvider extends BlockProvider {
    @Override
    public void onPlace(PlayerData data, int x, int y, int z, BlockFace face, Vector3f hit, int meta, WrappedBlockState state) {
        if (BlockFaces.isHorizontal(face)) {
            state.setFacing(face);
        }
    }

    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
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
}
