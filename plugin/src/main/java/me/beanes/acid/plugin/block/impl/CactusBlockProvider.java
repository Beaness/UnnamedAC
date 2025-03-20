package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.util.BoundingBox;

public class CactusBlockProvider extends BlockProvider {
    @Override
    public BoundingBox getCollisionBoundingBox(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        return new BoundingBox((float)x + 0.0625F, y, (float)z + 0.0625F, (float)(x + 1) - 0.0625F, (float)(y + 1) - 0.0625F, (float)(z + 1) - 0.0625F);
    }

    // Amazingly the select box is different from the block boundaries so raytrace hits this block even if you your look vector does not intersect with selection box (better for me no need to make custom bounds!)

    @Override
    public boolean isFullCube(WrappedBlockState state) {
        return false;
    }
}
