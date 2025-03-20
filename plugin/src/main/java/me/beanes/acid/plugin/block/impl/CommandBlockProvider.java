package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

public class CommandBlockProvider extends BlockProvider {

    @Override
    public SplitStateBoolean onActivate(PlayerData data, int x, int y, int z, WrappedBlockState state, boolean latest) {
        return data.getAbilitiesTracker().getCreativeMode(); // Technically this isnt good enough as the block entity could not be registered -> we don't track that so yeah... its whatever here ig
    }
}
