package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3f;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.util.BoundingBox;
import org.bukkit.Bukkit;

public class SnowBlockProvider extends BlockProvider {
    @Override
    public BoundingBox getCollisionBoundingBox(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        return new BoundingBox(x, y, z, x + 1.0F, y + ((float)(state.getLayers() - 1) * 0.125F), z + 1.0F);
    }

    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
       return new BoundingBox(0.0F, 0.0F, 0.0F, 1.0F, (float)state.getLayers()   / 8.0F, 1.0F);
    }

    @Override
    public boolean isFullCube(WrappedBlockState state) {
        return false;
    }
}
