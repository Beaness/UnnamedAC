package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

public class SignBlockProvider extends BlockProvider {
    @Override
    public SplitStateBoolean onActivate(PlayerData data, int x, int y, int z, WrappedBlockState state, boolean latest) {
        return SplitStateBoolean.TRUE;
    }

    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        if (BlockTags.WALL_SIGNS.contains(state.getType())) {
            BlockFace facing = state.getFacing();
            float f = 0.28125F;
            float f1 = 0.78125F;
            float f2 = 0.0F;
            float f3 = 1.0F;
            float f4 = 0.125F;

            switch (facing) {
                case NORTH:
                    return new BoundingBox(f2, f, 1.0F - f4, f3, f1, 1.0F);

                case SOUTH:
                    return new BoundingBox(f2, f, 0.0F, f3, f1, f4);

                case WEST:
                    return new BoundingBox(1.0F - f4, f, f2, 1.0F, f1, f3);

                case EAST:
                    return new BoundingBox(0.0F, f, f2, f4, f1, f3);
            }

            return new BoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        }

        return new BoundingBox(0.5F - 0.25F, 0.0F, 0.5F - 0.25F, 0.5F + 0.25F, 1.0F, 0.5F + 0.25F);
    }
}
