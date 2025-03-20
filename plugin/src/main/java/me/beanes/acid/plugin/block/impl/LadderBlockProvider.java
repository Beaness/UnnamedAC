package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3f;
import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.util.BlockFaces;
import me.beanes.acid.plugin.util.BoundingBox;

public class LadderBlockProvider extends BlockProvider {
    private static final float WIDTH = 0.125F;

    @Override
    public BoundingBox getCollisionBoundingBox(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        switch (state.getFacing()) {
            case NORTH:
                return new BoundingBox(x, y, z + (double) (1.0F - WIDTH), x + (double) 1.0F, y + (double) 1.0F, z + (double) 1.0F);

            case SOUTH:
                return new BoundingBox(x, y, z, x + (double) 1.0F, y + (double) 1.0F, z + (double) WIDTH);

            case WEST:
                return new BoundingBox(x + (double) (1.0F - WIDTH), y, z, x + (double) 1.0F, y + (double) 1.0F, z + (double) 1.0F);

            case EAST:
            default:
                return new BoundingBox(x, y, z, x + (double) WIDTH, y + (double) 1.0F, z + (double) 1.0F);
        }
    }

    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        switch (state.getFacing()) {
            case NORTH:
                return new BoundingBox(0.0F, 0.0F, 1.0F - 0.125F, 1.0F, 1.0F, 1.0F);

            case SOUTH:
                return new BoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.125F);

            case WEST:
                return new BoundingBox(1.0F - 0.125F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

            case EAST:
            default:
                return new BoundingBox(0.0F, 0.0F, 0.0F, 0.125F, 1.0F, 1.0F);
        }
    }

    @Override
    public void onPlace(PlayerData data, int x, int y, int z, BlockFace face, Vector3f hit, int meta, WrappedBlockState state) {
        if (BlockFaces.isHorizontal(face) && this.canBlockStay(data, x - face.getModX(), y, z - face.getModZ())) {
            state.setFacing(face);
        } else {
            for (BlockFace possible : BlockFaces.HORIZONTAL_PLANE_FACES) {
                if (this.canBlockStay(data, x - possible.getModX(), y, z - face.getModZ())) {
                    state.setFacing(possible);
                    return;
                }
            }

            state.setFacing(BlockFace.NORTH);
        }
   }

    @Override
    public boolean isFullCube(WrappedBlockState state) {
        return false;
    }

    protected boolean canBlockStay(PlayerData data, int x, int y, int z) {
        WrappedBlockState state = data.getWorldTracker().getBlock(x, y, z).getValue(); // We just use latest value as if it was a split state false the server normally auto resyncs every block you change

        return Acid.get().getBlockManager().isNormalCube(state);
    }
}