package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.enums.Type;
import com.github.retrooper.packetevents.util.Vector3f;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.util.BoundingBox;

public class SlabBlockProvider extends BlockProvider {
    @Override
    public BoundingBox getCollisionBoundingBox(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        if (state.getTypeData() == Type.BOTTOM) {
            return new BoundingBox(x, y, z, x + 1.0, y + 0.5F, z + 1.0);
        } else if (state.getTypeData() == Type.TOP) {
            return new BoundingBox(x, y + 0.5F, z, x + 1.0F, y + 1.0, z + 1.0);
        }

        return super.getCollisionBoundingBox(data, x, y, z, state);
    }

    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        if (state.getTypeData() == Type.BOTTOM) {
            return new BoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
        } else if (state.getTypeData() == Type.TOP) {
            return new BoundingBox(0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 1.0F);
        }

        return super.getBoundsBasedOnState(data, x, y, z, state);
    }

    @Override
    public boolean isFullCube(WrappedBlockState state) {
        return state.getTypeData() == Type.DOUBLE;
    }

    @Override
    public void onPlace(PlayerData data, int x, int y, int z, BlockFace face, Vector3f hit, int meta, WrappedBlockState state) {
        if (face != BlockFace.DOWN && (face == BlockFace.UP || (double) hit.getY() <= 0.5D)) {
            state.setTypeData(Type.BOTTOM);
        } else {
            state.setTypeData(Type.TOP);
        }
    }
}


