package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.util.BoundingBox;

public class SugarCaneBlockProvider extends BlockProvider {
    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        return new BoundingBox(0.5F - 0.375F, 0.0F, 0.5F - 0.375F, 0.5F + 0.375F, 1.0F, 0.5F + 0.375F);
    }
}
