package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

import java.util.List;

public class BrewingStandBlockProvider extends BlockProvider {
    @Override
    public void addPossibleCollisionBoxes(PlayerData data, int x, int y, int z, WrappedBlockState state, BoundingBox mask, List<BoundingBox> list) {
        BoundingBox top = getTop(x, y, z);

        if (mask.intersectsWith(top)) {
            list.add(top);
        }

        BoundingBox base = getBase(x, y, z);

        if (mask.intersectsWith(base)) {
            list.add(base);
        }
    }

    @Override
    public SplitStateBoolean isColliding(PlayerData data, int x, int y, int z, WrappedBlockState state, BoundingBox mask) {
        if (mask.intersectsWith(getTop(x, y, z))) {
            return SplitStateBoolean.TRUE;
        }

        if (mask.intersectsWith(getBase(x, y, z))) {
            return SplitStateBoolean.TRUE;
        }

        return SplitStateBoolean.FALSE;
    }

    private BoundingBox getTop(int x, int y, int z) {
        return new BoundingBox(x + (double) 0.4375F, y, z + (double) 0.4375F, x + (double) 0.5625F, y + (double) 0.875F, z + (double) 0.5625F);
    }

    private BoundingBox getBase(int x, int y, int z) {
        return new BoundingBox(x, y, z, x + (double) 1.0F, y + (double) 0.125F, z + (double) 1.0F);
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
        return SplitStateBoolean.TRUE;
    }
}
