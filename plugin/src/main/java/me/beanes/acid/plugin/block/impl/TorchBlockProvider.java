package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3f;
import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.util.BlockFaces;
import me.beanes.acid.plugin.util.BoundingBox;

public class TorchBlockProvider extends BlockProvider {
    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        BlockFace facing = state.getFacing();

        if (facing == BlockFace.EAST) {
            return new BoundingBox(0.0F, 0.2F, 0.5F - 0.15F, 0.15F * 2.0F, 0.8F, 0.5F + 0.15F);
        } else if (facing == BlockFace.WEST) {
            return new BoundingBox(1.0F - 0.15F * 2.0F, 0.2F, 0.5F - 0.15F, 1.0F, 0.8F, 0.5F + 0.15F);
        } else if (facing == BlockFace.SOUTH) {
            return new BoundingBox(0.5F - 0.15F, 0.2F, 0.0F, 0.5F + 0.15F, 0.8F, 0.15F * 2.0F);
        } else if (facing == BlockFace.NORTH) {
            return new BoundingBox(0.5F - 0.15F, 0.2F, 1.0F - 0.15F * 2.0F, 0.5F + 0.15F, 0.8F, 1.0F);
        } else {
            return new BoundingBox(0.5F - 0.1F, 0.0F, 0.5F - 0.1F, 0.5F + 0.1F, 0.6F, 0.5F + 0.1F);
        }
    }

    @Override
    public void onPlace(PlayerData data, int x, int y, int z, BlockFace face, Vector3f hit, int meta, WrappedBlockState state) {
        // TODO: use wall torch here
        if (this.canPlaceAt(data, x, y, z, face)) {
            state.setFacing(face);
            return;
        }

        for (BlockFace possible : BlockFaces.HORIZONTAL_PLANE_FACES) {
            BlockFace opposite = possible.getOppositeFace();
            if (isNormalCube(data.getWorldTracker().getBlock(x + opposite.getModX(), y + opposite.getModY(), z + opposite.getModZ()).getValue())) {
                state.setFacing(possible);
                return;
            }
        }
    }

    private boolean canPlaceAt(PlayerData data, int x, int y, int z, BlockFace face) {
        BlockFace opposite = face.getOppositeFace();

        x += opposite.getModX();
        y += opposite.getModY();
        z += opposite.getModZ();

        WrappedBlockState state = data.getWorldTracker().getBlock(x, y, z).getValue();

        return BlockFaces.isHorizontal(face) && isNormalCube(state) || face == BlockFace.UP && this.canPlaceOn(state);
    }

    private boolean isNormalCube(WrappedBlockState state) {
        return state.getType().isSolid() && Acid.get().getBlockManager().isFullBlock(state);
    }

    private boolean canPlaceOn(WrappedBlockState state) {
        if (Acid.get().getBlockManager().doesBlockHaveSolidTopSurface(state)) {
            return true;
        } else {
            return BlockTags.FENCES.contains(state.getType()) || state.getType() == StateTypes.GLASS || state.getType() == StateTypes.COBBLESTONE_WALL || BlockTags.GLASS_PANES.contains(state.getType());
        }
    }
}
