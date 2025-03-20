package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3f;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

public class RedstoneDiodeBlockProvider extends BlockProvider {
    @Override
    public BoundingBox getCollisionBoundingBox(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        return new BoundingBox(x, y, z, x + 1.0F, y + 0.125F, z + 1.0);
    }

    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        return new BoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
    }

    @Override
    public boolean isFullCube(WrappedBlockState state) {
        return false;
    }

    @Override
    public SplitStateBoolean onActivate(PlayerData data, int x, int y, int z, WrappedBlockState state, boolean latest) {
        boolean notAdventure = data.getStateTracker().getGameMode().getValue() != GameMode.ADVENTURE;
        boolean notAdventureOld = data.getStateTracker().getGameMode().getOldValue() != null ? data.getStateTracker().getGameMode().getOldValue() != GameMode.ADVENTURE : latest;

        return SplitStateBoolean.result(notAdventure, notAdventureOld);
    }

    @Override
    public void onPlace(PlayerData data, int x, int y, int z, BlockFace face, Vector3f hit, int meta, WrappedBlockState state) {
        state.setFacing(data.getRotationTracker().getHorizontalFacing().getOppositeFace());
    }
}
