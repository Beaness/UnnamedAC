package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

import java.util.List;

public class CauldronBlockProvider extends BlockProvider {

    private static final float SIDE_WIDTH = 0.125F;

    @Override
    public void addPossibleCollisionBoxes(PlayerData data, int x, int y, int z, WrappedBlockState state, BoundingBox mask, List<BoundingBox> list) {
        BoundingBox bottom = getBottom(x, y, z);
        if (mask.intersectsWith(bottom)) {
            list.add(bottom);
        }

        for (BoundingBox side : getSides(x, y, z)) {
            if (mask.intersectsWith(side)) {
                list.add(side);
            }
        }
    }

    @Override
    public SplitStateBoolean isColliding(PlayerData data, int x, int y, int z, WrappedBlockState state, BoundingBox mask) {
        if (mask.intersectsWith(getBottom(x, y, z))) {
            return SplitStateBoolean.TRUE;
        }

        for (BoundingBox side : getSides(x, y, z)) {
            if (mask.intersectsWith(side)) {
                return SplitStateBoolean.TRUE;
            }
        }

        return SplitStateBoolean.FALSE;
    }

    private BoundingBox getBottom(int x, int y, int z) {
        return new BoundingBox(x, y, z, x + (double) 1.0F, y + (double) 0.3125F, z + (double) 1.0F);
    }

    private BoundingBox[] getSides(int x, int y, int z) {
        return new BoundingBox[] {
                new BoundingBox(x, y, z, x + SIDE_WIDTH, y + (double) 1.0F, z + (double) 1.0F),
                new BoundingBox(x, y, z, x + (double) 1.0F, y + (double) 1.0F, z + (double) SIDE_WIDTH),
                new BoundingBox(x + (double) (1.0F - SIDE_WIDTH), y,  z, x + (double) 1.0F, y + (double) 1.0F, z + (double) 1.0F),
                new BoundingBox(x, y, z + (double) (1.0F - SIDE_WIDTH), x + (double) 1.0F, y + (double) 1.0F, z + (double) 1.0F)
        };
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
