package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3f;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

public class FurnaceBlockProvider extends BlockProvider {
    @Override
    public SplitStateBoolean onActivate(PlayerData data, int x, int y, int z, WrappedBlockState state, boolean latest) {
        return SplitStateBoolean.TRUE;
    }

    @Override
    public void onPlace(PlayerData data, int x, int y, int z, BlockFace face, Vector3f hit, int meta, WrappedBlockState state) {
        state.setFacing(data.getRotationTracker().getHorizontalFacing().getOppositeFace());
    }
}
