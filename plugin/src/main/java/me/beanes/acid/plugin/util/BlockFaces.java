package me.beanes.acid.plugin.util;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import me.beanes.acid.plugin.player.PlayerData;

public class BlockFaces {

    public static final BlockFace[] HORIZONTAL_PLANE_FACES = new BlockFace[]{ BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST }; // Order is important!
    public static final BlockFace[] VERTICAL_PLANE_FACES = new BlockFace[]{ BlockFace.UP, BlockFace.DOWN };

    public static boolean isHorizontal(BlockFace check) {
        for (BlockFace face : HORIZONTAL_PLANE_FACES) {
            if (face == check) {
                return true;
            }
        }

        return false;
    }

    public static boolean isVertical(BlockFace check) {
        for (BlockFace face : VERTICAL_PLANE_FACES) {
            if (face == check) {
                return true;
            }
        }

        return false;
    }

    public static BlockFace getFacing(PlayerData data, int x, int y, int z) {
        if (MCMath.abs((float)data.getPositionTracker().getX() - (float)x) < 2.0F && MCMath.abs((float)data.getPositionTracker().getZ() - (float)z) < 2.0F) {
            double eye = data.getPositionTracker().getY() + (data.getActionTracker().isSneaking() ? (double) (1.62F - 0.08F) : (double)1.62f);

            if (eye - (double)y > 2.0D) {
                return BlockFace.UP;
            }

            if ((double)y - eye > 0.0D) {
                return BlockFace.DOWN;
            }
        }

        return data.getRotationTracker().getHorizontalFacing().getOppositeFace();
    }

    public static BlockFace fromAngle(double angle) {
        int index = MCMath.floor_double(angle / 90.0D + 0.5D) & 3;
        return HORIZONTAL_PLANE_FACES[MCMath.abs_int(index % HORIZONTAL_PLANE_FACES.length)];
    }
}
