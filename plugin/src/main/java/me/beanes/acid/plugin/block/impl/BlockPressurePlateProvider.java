package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.util.BoundingBox;

public class BlockPressurePlateProvider extends BlockProvider {
    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        boolean weighted = state.getType() == StateTypes.HEAVY_WEIGHTED_PRESSURE_PLATE || state.getType() == StateTypes.LIGHT_WEIGHTED_PRESSURE_PLATE;

        boolean activated = weighted ? state.getPower() > 0 : state.isPowered();

        if (activated) {
            return new BoundingBox(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.03125F, 0.9375F);
        }  else {
            return new BoundingBox(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.0625F, 0.9375F);
        }
    }
}
