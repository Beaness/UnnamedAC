package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3f;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class HopperBlockProvider extends BlockProvider {
    private static final float SIDE_WIDTH = 0.125F;
    @Override
    public void addPossibleCollisionBoxes(PlayerData data, int x, int y, int z, WrappedBlockState state, BoundingBox mask, List<BoundingBox> list) {
        BoundingBox bottom = new BoundingBox(x, y, z, x + 1.0F, y + 0.625F, z + 1.0F);
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
        BoundingBox bottom = new BoundingBox(x, y, z, x + 1.0F, y + 0.625F, z + 1.0F);
        if (mask.intersectsWith(bottom)) {
            return SplitStateBoolean.TRUE;
        }

        for (BoundingBox side : getSides(x, y, z)) {
            if (mask.intersectsWith(side)) {
                return SplitStateBoolean.TRUE;
            }
        }

        return SplitStateBoolean.FALSE;
    }

    private BoundingBox[] getSides(int x, int y, int z) {
        return new BoundingBox[] {
                new BoundingBox(x, y, z, x + (double) SIDE_WIDTH, y + (double) 1.0F, z + (double) 1.0F),
                new BoundingBox(x, y, z, x + (double) 1.0F, y + (double) 1.0F, z + (double) SIDE_WIDTH),
                new BoundingBox(x + (double) (1.0F - SIDE_WIDTH), y,  z, x + (double) 1.0F, y + (double) 1.0F, z + (double) 1.0F),
                new BoundingBox(x, y, z + (double) (1.0F - SIDE_WIDTH), x + (double) 1.0F, y + (double) 1.0F, z + (double) 1.0F)
        };
    };

    @Override
    public SplitStateBoolean onActivate(PlayerData data, int x, int y, int z, WrappedBlockState state, boolean latest) {
        return SplitStateBoolean.TRUE;
    }

    @Override
    public void onPlace(PlayerData data, int x, int y, int z, BlockFace face, Vector3f hit, int meta, WrappedBlockState state) {
        BlockFace opposite = face.getOppositeFace();

        if (opposite == BlockFace.UP) {
            opposite = BlockFace.DOWN;
        }

        state.setFacing(opposite);
    }
}
