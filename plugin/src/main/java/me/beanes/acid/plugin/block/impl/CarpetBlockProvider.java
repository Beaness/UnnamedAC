package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.util.BoundingBox;

public class CarpetBlockProvider extends BlockProvider {
    @Override
    public BoundingBox getCollisionBoundingBox(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        if (data.getUser().getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_7_10)) {
            return new BoundingBox(x, y, z, x + 1.0F, y, z + 1.0F);
        } else {
            return new BoundingBox(x, y, z, x + 1.0F, y + 0.0625F, z + 1.0F);
        }
    }

    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        float f = 1F / 16.0F;
        return new BoundingBox(0.0F, 0.0F, 0.0F, 1.0F, f, 1.0F);
    }

    @Override
    public boolean isFullCube(WrappedBlockState state) {
        return false;
    }
}
