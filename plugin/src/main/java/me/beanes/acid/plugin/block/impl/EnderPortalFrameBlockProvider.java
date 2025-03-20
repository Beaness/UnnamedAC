package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3f;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

import java.util.List;

public class EnderPortalFrameBlockProvider extends BlockProvider {

    @Override
    public void addPossibleCollisionBoxes(PlayerData data, int x, int y, int z, WrappedBlockState state, BoundingBox mask, List<BoundingBox> list) {
        BoundingBox main = getMainBox(x, y, z);

        if (mask.intersectsWith(main)) {
            list.add(main);
        }

        if (state.isEye()) {
            BoundingBox eye = getEyeBox(x, y, z);
            if (mask.intersectsWith(eye)) {
                list.add(eye);
            }
        }
    }

    @Override
    public SplitStateBoolean isColliding(PlayerData data, int x, int y, int z, WrappedBlockState state, BoundingBox mask) {
        if (mask.intersectsWith(getMainBox(x, y, z))) {
            return SplitStateBoolean.TRUE;
        }

        if (state.isEye()) {
            if (mask.intersectsWith(getEyeBox(x, y, z))) {
                return SplitStateBoolean.TRUE;
            }
        }

        return SplitStateBoolean.FALSE;
    }

    private BoundingBox getMainBox(int x, int y, int z) {
        return new BoundingBox(x, y, z, x + (double) 1.0F, y + (double) 0.8125F, z + (double) 1.0F);
    }

    private BoundingBox getEyeBox(int x, int y, int z) {
        return new BoundingBox(x + (double) 0.3125F, y + (double) 0.8125F, z + (double) 0.3125F, x + (double) 0.6875F, y + (double) 1.0F, z + (double) 0.6875F);
    }

    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        return new BoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 0.8125F, 1.0F);
    }

    @Override
    public boolean isFullCube(WrappedBlockState state) {
        return false;
    }

    @Override
    public void onPlace(PlayerData data, int x, int y, int z, BlockFace face, Vector3f hit, int meta, WrappedBlockState state) {
        state.setFacing(data.getRotationTracker().getHorizontalFacing().getOppositeFace());
    }

    // Eye activation seems to be done serverside no need to simulate
}
