package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

public class EnchantmentTableBlockProvider extends BlockProvider {

    @Override
    public BoundingBox getCollisionBoundingBox(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        return new BoundingBox(x, y, z, x + (double) 1.0F, y + (double) 0.75F, z + (double) 1.0F);
    }

    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        return new BoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 0.75F, 1.0F);
    }

    @Override
    public boolean isFullCube(WrappedBlockState state) {
        return false;
    }

    @Override
    public SplitStateBoolean onActivate(PlayerData data, int x, int y, int z, WrappedBlockState state, boolean latest) {
        return SplitStateBoolean.TRUE;
    }
}
