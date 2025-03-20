package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.enums.Axis;
import com.github.retrooper.packetevents.util.Vector3f;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

import java.util.List;

public class AnvilBlockProvider extends BlockProvider {

    @Override
    public void addPossibleCollisionBoxes(PlayerData data, int x, int y, int z, WrappedBlockState state, BoundingBox mask, List<BoundingBox> list) {
        // Add all possibilities because anvils are just glitchy
        BoundingBox boxOne = new BoundingBox(x, y, z + (double) 0.125F, x + (double) 1.0F, y + (double) 1.0F, z + (double) 0.875F);
        BoundingBox boxTwo = new BoundingBox(x + (double) 0.125F, y, z, x + (double) 0.875F, y + (double) 1.0F, z + (double) 1.0F);

        if (mask.intersectsWith(boxOne)) {
            list.add(boxOne);
        }

        if (mask.intersectsWith(boxTwo)) {
            list.add(boxTwo);
        }
    }

    @Override
    public SplitStateBoolean isColliding(PlayerData data, int x, int y, int z, WrappedBlockState state, BoundingBox mask) {
        BoundingBox boxOne = new BoundingBox(x, y, z + (double) 0.125F, x + (double) 1.0F, y + (double) 1.0F, z + (double) 0.875F);
        BoundingBox boxTwo = new BoundingBox(x + (double) 0.125F, y, z, x + (double) 0.875F, y + (double) 1.0F, z + (double) 1.0F);

        boolean first = mask.intersectsWith(boxOne);
        boolean second = mask.intersectsWith(boxTwo);

        // We use a split state here because anvil colliding boxes are uncertain
        return SplitStateBoolean.result(first, second);
    }

    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        if (state.getFacing() == BlockFace.WEST || state.getFacing() == BlockFace.EAST) {
            return new BoundingBox(0.0F, 0.0F, 0.125F, 1.0F, 1.0F, 0.875F);
        } else {
            return new BoundingBox(0.125F, 0.0F, 0.0F, 0.875F, 1.0F, 1.0F);
        }
    }

    @Override
    public boolean isFullCube(WrappedBlockState state) {
        return false;
    }

    public void onPlace(PlayerData data, int x, int y, int z, BlockFace placeFace, Vector3f hit, int meta, WrappedBlockState state) {
        BlockFace face = data.getRotationTracker().getHorizontalFacing().getCW();

        if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
            state.setFacing(BlockFace.NORTH);
        } else if (face == BlockFace.WEST || face == BlockFace.EAST) {
            state.setFacing(BlockFace.WEST);
        }
    }

    @Override
    public SplitStateBoolean onActivate(PlayerData data, int x, int y, int z, WrappedBlockState state, boolean latest) {
        return SplitStateBoolean.TRUE;
    }
}
