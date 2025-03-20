package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.MaterialType;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.protocol.world.states.enums.Half;
import com.github.retrooper.packetevents.protocol.world.states.enums.Hinge;
import com.github.retrooper.packetevents.protocol.world.states.type.StateValue;
import com.github.retrooper.packetevents.util.Vector3f;
import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.util.BlockFaces;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.SplitState;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

import java.util.List;
import java.util.Locale;

public class DoorBlockProvider extends BlockProvider {
    private static final float DOOR_WIDTH = 0.1875F;

    /*
        Ok so after wasting my time with mojang stupid 1.8 door code, this is the conclusion
        A combined metadata is made using the first metadata from the top and bottom of the door
        The first 3 bits of the combined metadata is always the bottom metadata
        The next 3 bits of the combined metadata is always the top metadata

        Ok for the state of the door:
        Facing -> bit 1 & 2 of combined -> bottom metadata
        Open -> bit 3 of combined -> bottom metadata
        Is top -> bit 4 of normal meta -> any door block can have this set
        Is hinge left -> bit 5 of combined -> top metadata
     */

    @Override
    public void addPossibleCollisionBoxes(PlayerData data, int x, int y, int z, WrappedBlockState state, BoundingBox mask, List<BoundingBox> list) {
        SplitState<BoundingBox> splitState = getDoorBox(data, x, y, z, state);
        BoundingBox latest = splitState.getValue();
        BoundingBox old = splitState.getOldValue();

        if (mask.intersectsWith(latest)) {
            list.add(latest);
        }

        if (splitState.getOldValue() != null && mask.intersectsWith(old)) {
            list.add(old);
        }

    }

    @Override
    public SplitStateBoolean isColliding(PlayerData data, int x, int y, int z, WrappedBlockState state, BoundingBox mask) {
        SplitState<BoundingBox> splitState = getDoorBox(data, x, y, z, state);
        BoundingBox latestBox = splitState.getValue();
        BoundingBox oldBox = splitState.getOldValue();

        boolean latest = mask.intersectsWith(latestBox);
        boolean old = splitState.getOldValue() != null && mask.intersectsWith(oldBox);

        return SplitStateBoolean.result(latest, old);
    }

    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        return getDoorBox(data, 0, 0, 0, state).getValue(); // 0, 0, 0 hack to just get bounds
    }

    private SplitState<BoundingBox> getDoorBox(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        boolean isTop = state.getHalf() == Half.UPPER;

        SplitState<WrappedBlockState> otherBlockSplitState = data.getWorldTracker().getBlock(x, isTop ? (y - 1) : (y + 1), z);
        WrappedBlockState otherBlock = otherBlockSplitState.getValue();
        WrappedBlockState otherBlockOld = otherBlockSplitState.getOldValue();

        BlockFace facing;
        BlockFace oldFacing = null;

        // Facing is defined by the bottom block
        // If the facing is somehow not defined on the other block the index 0 (south) of horizontal block faces will be chosen and then rotated CCW which means east
        if (isTop) {
            facing = containsState(otherBlock, StateValue.FACING) ? otherBlock.getFacing() : BlockFace.EAST;

            if (otherBlockOld != null) {
                oldFacing = containsState(otherBlockOld, StateValue.FACING) ? otherBlockOld.getFacing() : BlockFace.EAST;

                // No point in using old value if it's the same
                if (oldFacing == facing) {
                    oldFacing = null;
                }
            }
        } else {
            facing = state.getFacing();
        }

        boolean open;
        Boolean oldOpen = null;

        // Open is defined on the bottom block
        if (isTop) {
            open = containsState(otherBlock, StateValue.OPEN) && otherBlock.isOpen();

            if (otherBlockOld != null) {
                oldOpen = containsState(otherBlockOld, StateValue.OPEN) && otherBlockOld.isOpen();

                // No point in using old value if it's the same
                if (oldOpen == open) {
                    oldOpen = null;
                }
            }
        } else {
            open = state.isOpen();
        }

        Hinge hinge;
        Hinge oldHinge = null;

        // Hinge is defined on top metadata, default value is right
        if (isTop) {
            hinge = state.getHinge();
        } else {
            hinge = containsState(otherBlock, StateValue.HINGE) ? otherBlock.getHinge() : Hinge.RIGHT;

            if (otherBlockOld != null) {
                oldHinge = containsState(otherBlockOld, StateValue.HINGE) ? otherBlockOld.getHinge() : Hinge.RIGHT;

                // No point in using old value if it's the same
                if (oldHinge == hinge) {
                    oldHinge = null;
                }
            }
        }

        BoundingBox latest = getBoxForFacing(x, y, z, getFacing(facing, open, hinge == Hinge.LEFT));

        BoundingBox old = null;
        if (oldFacing != null || oldOpen != null || oldHinge != null) {
            old = getBoxForFacing(x, y, z, getFacing(oldFacing != null ? oldFacing : facing, oldOpen != null ? oldOpen : open, oldHinge != null ? oldHinge == Hinge.LEFT : hinge == Hinge.LEFT));
        }

        return new SplitState<>(latest, old);
    }

    private boolean containsState(WrappedBlockState state, StateValue stateValue) {
        return state.getInternalData().containsKey(stateValue); // TODO: replace this with a non deprecated method
    }

    @Override
    public boolean isFullCube(WrappedBlockState state) {
        return false;
    }

    private BlockFace getFacing(BlockFace facing, boolean open, boolean hingeLeft) {
        switch (facing) {
            case SOUTH:
                return open ? (hingeLeft ? BlockFace.WEST : BlockFace.EAST) : BlockFace.SOUTH;
            case WEST:
                return open ? (hingeLeft ? BlockFace.NORTH : BlockFace.SOUTH) : BlockFace.WEST;
            case NORTH:
                return open ? (hingeLeft ? BlockFace.EAST : BlockFace.WEST) : BlockFace.NORTH;
            default:
                return open ? (hingeLeft ? BlockFace.SOUTH : BlockFace.NORTH) : BlockFace.EAST;
        }
    }

    private BoundingBox getBoxForFacing(int x, int y, int z, BlockFace facing) {
        switch (facing) {
            case SOUTH:
                return new BoundingBox(x, y, z, x + (double) 1.0F, y + (double) 1.0F, z + (double) DOOR_WIDTH);
            case WEST:
                return new BoundingBox(x + (double) (1.0F - DOOR_WIDTH), y, z, x + (double) 1.0F, y + (double) 1.0F, z + (double) 1.0F);
            case NORTH:
                return new BoundingBox(x, y, z + (double) (1.0F - DOOR_WIDTH), x + (double) 1.0F, y + (double) 1.0F, z + (double) 1.0F);
            default:
                return new BoundingBox(x, y, z, x + (double) DOOR_WIDTH, y + (double) 1.0F, z + (double) 1.0F);
        }
    }

    @Override
    public SplitStateBoolean onActivate(PlayerData data, int x, int y, int z, WrappedBlockState state, boolean latest) {
        if (state.getType().getMaterialType() == MaterialType.METAL) {
            return SplitStateBoolean.TRUE;
        }

        if (state.getHalf() != Half.LOWER) {
            y -= 1;
        }

        SplitState<WrappedBlockState> splitState = data.getWorldTracker().getBlock(x, y, z);
        WrappedBlockState latestState = splitState.getValue();
        WrappedBlockState oldState = splitState.getOldValue();

        boolean activateLatest = latestState.getType() == state.getType();

        if (activateLatest) {
            latestState.setOpen(!latestState.isOpen());
            data.getWorldTracker().setBlock(x, y, z, latestState);
        }

        if (oldState != null) {
            boolean activateOld = oldState.getType() == state.getType();

            if (activateOld) {
                oldState.setOpen(!oldState.isOpen());
                data.getWorldTracker().setOldBlock(x, y, z, oldState);
            }

            if (activateLatest != activateOld) {
                return SplitStateBoolean.POSSIBLE;
            }
        }

        return activateLatest ? SplitStateBoolean.TRUE : SplitStateBoolean.FALSE;
    }

    @Override
    public void onPlace(PlayerData data, int x, int y, int z, BlockFace face, Vector3f hit, int meta, WrappedBlockState state) {
        // Copied logic from ItemDoor
        BlockFace facing = BlockFaces.fromAngle(data.getRotationTracker().getYaw());

        BlockFace facingCW = facing.getCW();
        BlockFace facingCCW = facing.getCCW();

        WrappedBlockState first = data.getWorldTracker().getBlock(x + facingCW.getModX(), y + facingCW.getModY(), z + facing.getModZ()).getValue();
        WrappedBlockState firstUp = data.getWorldTracker().getBlock(x + facingCW.getModX(), y + facingCW.getModY() + 1, z + facing.getModZ()).getValue();
        WrappedBlockState second = data.getWorldTracker().getBlock(x + facingCCW.getModX(), y + facingCCW.getModY(), z + facingCCW.getModZ()).getValue();
        WrappedBlockState secondUp = data.getWorldTracker().getBlock(x + facingCCW.getModX(), y + facingCCW.getModY() + 1, z + facingCCW.getModZ()).getValue();

        int i = (Acid.get().getBlockManager().isNormalCube(second) ? 1 : 0) + (Acid.get().getBlockManager().isNormalCube(secondUp) ? 1 : 0);
        int j = (Acid.get().getBlockManager().isNormalCube(first) ? 1 : 0) + (Acid.get().getBlockManager().isNormalCube(first) ? 1 : 0);
        boolean secondDoor = BlockTags.DOORS.contains(second.getType()) || BlockTags.DOORS.contains(secondUp.getType());
        boolean firstDoor = BlockTags.DOORS.contains(first.getType()) || BlockTags.DOORS.contains(firstUp.getType());
        boolean hingeRight = secondDoor && !firstDoor || j > i;

        state.setFacing(facing);
        state.setHalf(Half.LOWER);
        state.setHinge(hingeRight ? Hinge.RIGHT : Hinge.LEFT);

        // This is how grim circumvents packetevents limitation of multi version states :P
        String name = state.getType().getName().toLowerCase(Locale.ROOT);
        String hinge = (hingeRight ? Hinge.RIGHT : Hinge.LEFT).toString().toLowerCase(Locale.ROOT);
        String str = "minecraft:" + name + "[half=upper,hinge=" + hinge + "]";
        WrappedBlockState upperState = WrappedBlockState.getByString(data.getUser().getClientVersion(), str);

        data.getWorldTracker().setBlock(x, y + 1, z, upperState);
    }
}
