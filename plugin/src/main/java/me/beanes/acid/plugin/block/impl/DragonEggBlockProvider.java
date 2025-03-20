package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

public class DragonEggBlockProvider extends BlockProvider {
    @Override
    public BoundingBox getCollisionBoundingBox(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        return new BoundingBox(x + (double) 0.0625F, y, z + (double) 0.0625F, x + (double) 0.9375F, y + (double) 1.0F, z + (double) 0.9375F);
    }

    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        return new BoundingBox(0.0625F, 0.0F, 0.0625F, 0.9375F, 1.0F, 0.9375F);
    }

    @Override
    public boolean isFullCube(WrappedBlockState state) {
        return false;
    }

    @Override
    public SplitStateBoolean onActivate(PlayerData data, int x, int y, int z, WrappedBlockState state, boolean latest) {
        if (latest) {
            data.getWorldTracker().setBlock(x, y, z, WrappedBlockState.getByGlobalId(0));
        } else {
            data.getWorldTracker().setOldBlock(x, y, z, WrappedBlockState.getByGlobalId(0));
        }

        return SplitStateBoolean.TRUE;
    }
}
