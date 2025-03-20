package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

public class JukeboxBlockProvider extends BlockProvider {
    @Override
    public SplitStateBoolean onActivate(PlayerData data, int x, int y, int z, WrappedBlockState state, boolean latest) {
        if (state.isHasRecord()) {
            state.setHasRecord(false);

            if (latest) {
                data.getWorldTracker().setBlock(x, y, z, state);
            } else {
                data.getWorldTracker().setOldBlock(x, y, z, state);
            }

            return SplitStateBoolean.TRUE;
        }

        return SplitStateBoolean.FALSE;
    }
}
