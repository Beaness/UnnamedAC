package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.enums.Axis;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.util.BoundingBox;

public class NetherPortalBlockProvider extends BlockProvider {
    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        Axis axis = state.getAxis();
        float modX = 0.125F;
        float modZ = 0.125F;

        if (axis == Axis.X) {
            modX = 0.5F;
        } else if (axis == Axis.Z) {
            modZ = 0.5F;
        }

        return new BoundingBox(0.5F - modX, 0.0F, 0.5F - modZ, 0.5F + modX, 1.0F, 0.5F + modZ);
    }
}
