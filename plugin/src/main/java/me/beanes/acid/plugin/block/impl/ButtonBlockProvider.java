package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.enums.Face;
import com.github.retrooper.packetevents.util.Vector3f;
import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.util.BlockFaces;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

public class ButtonBlockProvider extends BlockProvider {
    @Override
    public SplitStateBoolean onActivate(PlayerData data, int x, int y, int z, WrappedBlockState state, boolean latest) {
        return SplitStateBoolean.TRUE;
    }

    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        Face face = state.getFace();
        boolean powered = state.isPowered();
        float widthModifier = (float)(powered ? 1 : 2) / 16.0F;

        if (face == Face.FLOOR) {
            return new BoundingBox(0.3125F, 0.0F, 0.375F, 0.6875F, 0.0F + widthModifier, 0.625F);
        } else if (face == Face.CEILING) {
            return new BoundingBox(0.3125F, 1.0F - widthModifier, 0.375F, 0.6875F, 1.0F, 0.625F);
        } else {
            BlockFace facing = state.getFacing();
            switch (facing) {
                case EAST:
                    return new BoundingBox(0.0F, 0.375F, 0.3125F, widthModifier, 0.625F, 0.6875F);

                case WEST:
                    return new BoundingBox(1.0F - widthModifier, 0.375F, 0.3125F, 1.0F, 0.625F, 0.6875F);

                case SOUTH:
                    return new BoundingBox(0.3125F, 0.375F, 0.0F, 0.6875F, 0.625F, widthModifier);

                case NORTH:
                default:
                    return new BoundingBox(0.3125F, 0.375F, 1.0F - widthModifier, 0.6875F, 0.625F, 1.0F);
            }
        }
    }

    @Override
    public void onPlace(PlayerData data, int x, int y, int z, BlockFace face, Vector3f hit, int meta, WrappedBlockState state) {
        BlockFace opposite = face.getOppositeFace();
        WrappedBlockState targetState = data.getWorldTracker().getBlock(x + opposite.getModX(), y + opposite.getModY(), z + opposite.getModZ()).getValue();

        if (BlockFaces.isHorizontal(face)) {
            state.setFace(Face.WALL);
        } else if (face == BlockFace.UP) {
            state.setFace(Face.FLOOR);
        } else if (face == BlockFace.DOWN) {
            state.setFace(Face.CEILING);
        }

        boolean canStay = opposite == BlockFace.DOWN
                ? Acid.get().getBlockManager().doesBlockHaveSolidTopSurface(targetState)
                : Acid.get().getBlockManager().isNormalCube(targetState);

        if (canStay) {
            state.setFacing(face);
            return;
        }

        state.setFacing(BlockFace.DOWN);
    }
}
