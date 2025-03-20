package me.beanes.acid.plugin.util;

import com.github.retrooper.packetevents.protocol.world.MaterialType;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;

public class BlockUtil {

    // This class contains utility methods for blocks used by minecraft
    // only methods that do not need BlockProviders should be put in here

    public static boolean isSolid(MaterialType materialType) {
        // Air is not solid
        if (materialType == MaterialType.AIR) {
            return false;
        }

        // Fire is not solid
        if (materialType == MaterialType.FIRE) {
            return false;
        }

        // Liquid is not solid
        if (materialType == MaterialType.WATER || materialType == MaterialType.LAVA) {
            return false;
        }

        // Snow is not solid
        if (materialType == MaterialType.TOP_SNOW) {
            return false;
        }

        // Decorations are not solid
        if (materialType == MaterialType.DECORATION) {
            return false;
        }

        // Vines are not solid
        if (materialType == MaterialType.REPLACEABLE_PLANT) {
            return false;
        }

        // Portals are not solid
        if (materialType == MaterialType.PORTAL) {
            return false;
        }

        // Plants/carpets are not solid
        if (materialType == MaterialType.PLANT) {
            return false;
        }

        // Webs are not solid
        if (materialType == MaterialType.WEB) {
            return false;
        }

        return true;
    }

    public static boolean isBlockReplaceable(WrappedBlockState state) {
        StateType type = state.getType();

        if (type == StateTypes.SNOW) {
            return state.getLayers() == 1;
        }

        return type == StateTypes.AIR || type == StateTypes.WATER || type == StateTypes.LAVA || type == StateTypes.FERN || type == StateTypes.DEAD_BUSH || type == StateTypes.LARGE_FERN || type == StateTypes.TALL_GRASS || type == StateTypes.VINE || type == StateTypes.GRASS;
    }

    public static boolean canCollide(WrappedBlockState state, boolean hitIfLiquid) {
        StateType type = state.getType();

        if (type == StateTypes.FIRE) {
            return false;
        }

        if (type == StateTypes.AIR) {
            return false;
        }

        if (type == StateTypes.LAVA || type == StateTypes.WATER) {
            return hitIfLiquid && (state.getLevel() == 0);
        }

        return true;
    }

    public static boolean isBreakable(StateType type) {
        if (type == StateTypes.AIR) {
            return false;
        }

        if (type.getHardness() == -1.0F) {
            return false;
        }

        return type != StateTypes.WATER && type != StateTypes.LAVA;
    }
}
