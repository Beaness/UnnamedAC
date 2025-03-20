package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.util.BoundingBox;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BannerBlockProvider extends BlockProvider {
    private Set<StateType> HANGING_BANNERS = new HashSet<>(
            Arrays.asList(
                    StateTypes.WHITE_WALL_BANNER, StateTypes.ORANGE_WALL_BANNER, StateTypes.MAGENTA_WALL_BANNER, StateTypes.LIGHT_BLUE_WALL_BANNER, StateTypes.YELLOW_WALL_BANNER, StateTypes.LIME_WALL_BANNER, StateTypes.PINK_WALL_BANNER, StateTypes.GRAY_WALL_BANNER, StateTypes.LIGHT_GRAY_WALL_BANNER, StateTypes.CYAN_WALL_BANNER, StateTypes.PURPLE_WALL_BANNER, StateTypes.BLUE_WALL_BANNER, StateTypes.BROWN_WALL_BANNER, StateTypes.GREEN_WALL_BANNER, StateTypes.RED_WALL_BANNER, StateTypes.BLACK_WALL_BANNER
            )
    );

    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        if (HANGING_BANNERS.contains(state.getType())) {
            BlockFace face = state.getFacing();

            switch (face) {
                case SOUTH:
                    return new BoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 0.78125F, 0.125F);

                case WEST:
                    return new BoundingBox(1.0F - 0.125F, 0.0F, 0.0F, 1.0F, 0.78125F, 1.0F);

                case EAST:
                    return new BoundingBox(0.0F, 0.0F, 0.0F, 0.125F, 0.78125F, 1.0F);
                default:
                case NORTH:
                    return new BoundingBox(0.0F, 0.0F, 1.0F - 0.125F, 1.0F, 0.78125F, 1.0F);

            }
        } else {
            return new BoundingBox(0.5F - 0.25F, 0.0F, 0.5F - 0.25F, 0.5F + 0.25F, 1.0F, 0.5F + 0.25F);
        }
    }
}
