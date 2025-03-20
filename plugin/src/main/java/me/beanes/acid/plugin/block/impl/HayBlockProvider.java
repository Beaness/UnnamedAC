package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.enums.Axis;
import com.github.retrooper.packetevents.util.Vector3f;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.player.PlayerData;

public class HayBlockProvider extends BlockProvider {
    @Override
    public void onPlace(PlayerData data, int x, int y, int z, BlockFace face, Vector3f hit, int meta, WrappedBlockState state) {
        if (face == BlockFace.UP || face == BlockFace.DOWN) {
            state.setAxis(Axis.Y);
        } else if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
            state.setAxis(Axis.Z);
        } else if (face == BlockFace.WEST || face == BlockFace.EAST) {
            state.setAxis(Axis.X);
        }
    }
}
