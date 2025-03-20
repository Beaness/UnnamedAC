package me.beanes.acid.plugin.player.tracker.impl.world;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.enums.Type;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.util.BlockFaces;
import me.beanes.acid.plugin.util.BlockUtil;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.MCMath;
import org.bukkit.Bukkit;

// Hacky static class file to handle all items / blocks that don't use vanilla block rules and have overriden behaivour
public class CustomBlockPlaces {
    public static boolean canPlaceFlowerPot(PlayerData data, int x, int y, int z) {
        WrappedBlockState oldState = data.getWorldTracker().getBlock(x, y, z).getValue();

        // TODO: do bounding check!
        boolean replace = (!oldState.getType().isAir() && BlockUtil.isBlockReplaceable(oldState));
        boolean surface = Acid.get().getBlockManager().doesBlockHaveSolidTopSurface(data.getWorldTracker().getBlock(x, y - 1, z).getValue());

        return replace || surface;
    }

    public static boolean canPlaceDoor(PlayerData data, BlockFace face, int x, int y, int z) {
        if (face != BlockFace.UP) return false;

        boolean surface = Acid.get().getBlockManager().doesBlockHaveSolidTopSurface(data.getWorldTracker().getBlock(x, y - 1, z).getValue());
        WrappedBlockState self = data.getWorldTracker().getBlock(x, y, z).getValue();
        WrappedBlockState up = data.getWorldTracker().getBlock(x, y, z).getValue();

        return y < 255 && surface && BlockUtil.isBlockReplaceable(self) && BlockUtil.isBlockReplaceable(up);
    }

    public static boolean canPlaceRedstone(PlayerData data, int x, int y, int z) {
        WrappedBlockState blockDown = data.getWorldTracker().getBlock(x, y - 1, z).getValue();

        return blockDown.getType() == StateTypes.GLOWSTONE || Acid.get().getBlockManager().doesBlockHaveSolidTopSurface(blockDown);
    }

    public static boolean canPlaceRail(PlayerData data, int x, int y, int z) {
        WrappedBlockState blockDown = data.getWorldTracker().getBlock(x, y - 1, z).getValue();

        return Acid.get().getBlockManager().doesBlockHaveSolidTopSurface(blockDown);
    }

    // TODO: Add support for checking if an entity is there blocking the box -> this is more painful as lag can make the entity positions very uncertain
    public static boolean placeSlab(PlayerData data, BlockFace face, StateType in, int x, int y, int z) {
        WrappedBlockState state = data.getWorldTracker().getBlock(x, y, z).getValue();


        boolean uncertain = data.getEntityTracker().isEntityCollisionPresent(new BoundingBox(x, y, z, x + 1.0D, y + 1.0D , z + 1.0D)).possible();
        if (state.getType().equals(in)) {
            if (state.getTypeData() == Type.BOTTOM || state.getTypeData() == Type.TOP) {
                if ((face == BlockFace.UP && state.getTypeData() == Type.BOTTOM) || (face == BlockFace.DOWN && state.getTypeData() == Type.TOP)) {
                    if (uncertain) {
                        data.getWorldTracker().getResyncHandler().scheduleResync(x, y, z); // Resync because the collision present is just way too dumb and uncertain
                        return true;
                    }

                    state.setTypeData(Type.DOUBLE);
                    data.getWorldTracker().setBlock(x, y, z, state);
                    data.getInventoryTracker().removeOneItemFromHeldItemIfNotCreative();
                    return true;
                }
            }
        }


        WrappedBlockState other = data.getWorldTracker().getBlock(x + face.getModX(), y + face.getModY(), z + face.getModZ()).getValue();

        if (other.getType().equals(in)) {
            if (other.getTypeData() == Type.BOTTOM || other.getTypeData() == Type.TOP) {
                if (uncertain) {
                    data.getWorldTracker().getResyncHandler().scheduleResync(x, y, z); // Resync because the collision present is just way too dumb and uncertain
                    return true;
                }

                other.setTypeData(Type.DOUBLE);
                data.getWorldTracker().setBlock(x + face.getModX(), y + face.getModY(), z + face.getModZ(), other);
                data.getInventoryTracker().removeOneItemFromHeldItemIfNotCreative();
                return true;
            }
        }

        return false;
    }

    public static boolean placeSnow(PlayerData data, BlockFace face, int x, int y, int z) {
        if (face == BlockFace.OTHER) {
            return true;
        }

        WrappedBlockState currentState = data.getWorldTracker().getBlock(x, y, z).getValue();

        if ((face != BlockFace.UP) && !BlockUtil.isBlockReplaceable(currentState)) {
            x += face.getModX();
            y += face.getModY();
            z += face.getModZ();

            currentState = data.getWorldTracker().getBlock(x, y, z).getValue();
        }


        if (currentState.getType() == StateTypes.SNOW) {
            if (data.getEntityTracker().isEntityCollisionPresent(new BoundingBox(x, y, z, x + 1.0D, y + 1.0D , z + 1.0D)).possible()) {
                data.getWorldTracker().getResyncHandler().scheduleResync(x, y, z); // Resync because the collision present is just way too dumb and uncertain
                return true;
            }

            data.getInventoryTracker().removeOneItemFromHeldItemIfNotCreative();
            currentState.setLayers(currentState.getLayers() + 1);

            data.getWorldTracker().setBlock(x, y, z, currentState);
            return true;
        }


        return false;
    }

    public static void placeCocoa(PlayerData data, BlockFace face, int x, int y, int z) {
        if (!BlockFaces.isHorizontal(face)) {
            return;
        }

        WrappedBlockState current = data.getWorldTracker().getBlock(x, y, z).getValue();

        if (current.getType() == StateTypes.JUNGLE_LOG) {
            int targetX = x + face.getModX();
            int targetY = y + face.getModY();
            int targetZ = z + face.getModZ();
            WrappedBlockState target = data.getWorldTracker().getBlock(targetX, targetY, targetZ).getValue();

            if (target.getType().isAir()) {
                WrappedBlockState state = WrappedBlockState.getDefaultState(data.getUser().getClientVersion(), StateTypes.COCOA);
                Acid.get().getBlockManager().onPlace(data, targetX, targetY, targetZ, face, null, 0, state);
                data.getWorldTracker().setBlock(targetX, targetY, targetZ, state);
                data.getInventoryTracker().removeOneItemFromHeldItemIfNotCreative();
            }
        }
    }

    public static void rayTraceLily(PlayerData data) {
        BlockRayHit hit = doRayTrace(data, true);

        if (hit != null) {
            int x = hit.getX();
            int y = hit.getY();
            int z = hit.getZ();

            // Normally checks for world border and stuff to see if it can be modified but uh cba rn
            WrappedBlockState self = data.getWorldTracker().getBlock(x, y, z).getValue();
            WrappedBlockState up = data.getWorldTracker().getBlock(x, y + 1, z).getValue();

            if (self.getType() == StateTypes.WATER && up.getType() == StateTypes.AIR && self.getLevel() == 0) {
                data.getWorldTracker().setBlock(x, y + 1, z, WrappedBlockState.getDefaultState(data.getUser().getClientVersion(), StateTypes.LILY_PAD));
                data.getInventoryTracker().removeOneItemFromHeldItemIfNotCreative();
            }
        }
    }

    public static void rayTraceEmptyBucket(PlayerData data) {
        BlockRayHit hit = doRayTrace(data, true);

        if (hit != null) {
            int x = hit.getX();
            int y = hit.getY();
            int z = hit.getZ();

            // Normally checks for world border and stuff to see if it can be modified but uh cba rn
            WrappedBlockState block = data.getWorldTracker().getBlock(x, y, z).getValue();

            if (block.getType() == StateTypes.WATER || block.getType() == StateTypes.LAVA) {
                if (block.getLevel() != 0) {
                    return;
                }

                data.getWorldTracker().setBlock(x, y, z, WrappedBlockState.getDefaultState(StateTypes.AIR));
                if (data.getStateTracker().isCreative().notPossible()) {
                    ItemStack itemStack = ItemStack.builder()
                            .amount(1)
                            .type(block.getType() == StateTypes.WATER ? ItemTypes.WATER_BUCKET : ItemTypes.LAVA_BUCKET)
                            .build();

                    data.getInventoryTracker().getHeldItem().setValueCertainly(itemStack);
                }
            }
        }
    }

    public static void rayTraceFullBucket(PlayerData data, StateType type) {
        BlockRayHit hit = doRayTrace(data, false);

        if (hit != null) {
            int x = hit.getX() + hit.getPlane().getModX();
            int y = hit.getY() + hit.getPlane().getModY();
            int z = hit.getZ() + hit.getPlane().getModZ();

            // Normally checks for world border and stuff to see if it can be modified but uh cba rn
            WrappedBlockState block = data.getWorldTracker().getBlock(x, y, z).getValue();

            if (block.getType() == StateTypes.AIR || !BlockUtil.isSolid(block.getType().getMaterialType())) {
                data.getWorldTracker().setBlock(x, y, z, WrappedBlockState.getDefaultState(data.getUser().getClientVersion(), type));

                if (data.getStateTracker().isCreative().notPossible()) {
                    ItemStack itemStack = ItemStack.builder()
                            .amount(1)
                            .type(ItemTypes.BUCKET)
                            .build();

                    data.getInventoryTracker().getHeldItem().setValueCertainly(itemStack);
                }
            }
        }
    }


    public static BlockRayHit doRayTrace(PlayerData data, boolean liquid) {
        float pitch = data.getRotationTracker().getPitch();
        float yaw = data.getRotationTracker().getYaw();
        // Last coordinates as we wait for next client tick for accurate look
        double startX = data.getPositionTracker().getLastX();
        double startY = data.getPositionTracker().getLastY() + (double) (data.getActionTracker().isSneaking() ? (1.62F - 0.08F) : 1.62f);
        double startZ = data.getPositionTracker().getLastZ();
        Vector3d start = new Vector3d(startX, startY, startZ);
        float f2 = data.getTrigHandler().cos(-yaw * MCMath.DEGREES_TO_RADIANS - (float)Math.PI);
        float f3 = data.getTrigHandler().sin(-yaw * MCMath.DEGREES_TO_RADIANS - (float)Math.PI);
        float f4 = -data.getTrigHandler().cos(-pitch * MCMath.DEGREES_TO_RADIANS);
        float f5 = data.getTrigHandler().sin(-pitch * MCMath.DEGREES_TO_RADIANS);
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        double d3 = 5.0D;
        Vector3d end = start.add((double)f6 * d3, (double)f5 * d3, (double)f7 * d3);

        return data.getWorldTracker().rayTraceBlocks(start, end, liquid, !liquid, false);
    }
}
