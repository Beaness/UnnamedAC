package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.enums.East;
import com.github.retrooper.packetevents.protocol.world.states.enums.North;
import com.github.retrooper.packetevents.protocol.world.states.enums.South;
import com.github.retrooper.packetevents.protocol.world.states.enums.West;
import com.github.retrooper.packetevents.util.Vector3f;
import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.util.BlockFaces;
import me.beanes.acid.plugin.util.BlockUtil;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.SplitState;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

import java.util.List;

public class VineBlockProvider extends BlockProvider {
    @Override
    public void addPossibleCollisionBoxes(PlayerData data, int x, int y, int z, WrappedBlockState state, BoundingBox mask, List<BoundingBox> list) {
        float minX = 1.0F;
        float minY = 1.0F;
        float minZ = 1.0F;
        float maxX = 0.0F;
        float maxY = 0.0F;
        float maxZ = 0.0F;
        boolean directed = false;

        if (state.getWest() == West.TRUE) {
            maxX = 0.0625F;
            minX = 0.0F;
            minY = 0.0F;
            maxY = 1.0F;
            minZ = 0.0F;
            maxZ = 1.0F;
            directed = true;
        }

        if (state.getEast() == East.TRUE) {
            minX = Math.min(minX, 0.9375F);
            maxX = 1.0F;
            minY = 0.0F;
            maxY = 1.0F;
            minZ = 0.0F;
            maxZ = 1.0F;
            directed = true;
        }

        if (state.getNorth() == North.TRUE) {
            maxZ = Math.max(maxZ, 0.0625F);
            minZ = 0.0F;
            minX = 0.0F;
            maxX = 1.0F;
            minY = 0.0F;
            maxY = 1.0F;
            directed = true;
        }

        if (state.getSouth() == South.TRUE) {
            minZ = Math.min(minZ, 0.9375F);
            maxZ = 1.0F;
            minX = 0.0F;
            maxX = 1.0F;
            minY = 0.0F;
            maxY = 1.0F;
            directed = true;
        }

        if (!directed) {
            SplitStateBoolean placeOnState = this.canPlaceOn(data.getWorldTracker().getBlock(x, y + 1, z));
            if (placeOnState == SplitStateBoolean.TRUE) {
                BoundingBox box = new BoundingBox(x, y + (double) 0.9375F, z, x + (double) 1.0F, y + (double) 1.0F, z + (double) 1.0F);

                if (mask.intersectsWith(box)) {
                    list.add(box);
                }

                return;
            } else if (placeOnState == SplitStateBoolean.POSSIBLE) {
                BoundingBox box = new BoundingBox(x, y + (double) 0.9375F, z, x + (double) 1.0F, y + (double) 1.0F, z + (double) 1.0F);

                if (mask.intersectsWith(box)) {
                    list.add(box);
                }

                BoundingBox otherBox = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);

                if (mask.intersectsWith(otherBox)) {
                    list.add(otherBox);
                }

                return;
            }
        }

        BoundingBox box = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);

        if (mask.intersectsWith(box)) {
            list.add(box);
        }
    }

    @Override
    public SplitStateBoolean isColliding(PlayerData data, int x, int y, int z, WrappedBlockState state, BoundingBox mask) {
        float minX = 1.0F;
        float minY = 1.0F;
        float minZ = 1.0F;
        float maxX = 0.0F;
        float maxY = 0.0F;
        float maxZ = 0.0F;
        boolean directed = false;

        if (state.getWest() == West.TRUE) {
            maxX = 0.0625F;
            minX = 0.0F;
            minY = 0.0F;
            maxY = 1.0F;
            minZ = 0.0F;
            maxZ = 1.0F;
            directed = true;
        }

        if (state.getEast() == East.TRUE) {
            minX = Math.min(minX, 0.9375F);
            maxX = 1.0F;
            minY = 0.0F;
            maxY = 1.0F;
            minZ = 0.0F;
            maxZ = 1.0F;
            directed = true;
        }

        if (state.getNorth() == North.TRUE) {
            maxZ = Math.max(maxZ, 0.0625F);
            minZ = 0.0F;
            minX = 0.0F;
            maxX = 1.0F;
            minY = 0.0F;
            maxY = 1.0F;
            directed = true;
        }

        if (state.getSouth() == South.TRUE) {
            minZ = Math.min(minZ, 0.9375F);
            maxZ = 1.0F;
            minX = 0.0F;
            maxX = 1.0F;
            minY = 0.0F;
            maxY = 1.0F;
            directed = true;
        }

        if (!directed) {
            SplitStateBoolean placeOnState = this.canPlaceOn(data.getWorldTracker().getBlock(x, y + 1, z));
            if (placeOnState == SplitStateBoolean.TRUE) {
                BoundingBox box = new BoundingBox(x, y + (double) 0.9375F, z, x + (double) 1.0F, y + (double) 1.0F, z + (double) 1.0F);

                if (mask.intersectsWith(box)) {
                    return SplitStateBoolean.TRUE;
                }

                return SplitStateBoolean.FALSE;
            } else if (placeOnState == SplitStateBoolean.POSSIBLE) {
                BoundingBox box = new BoundingBox(x, y + (double) 0.9375F, z, x + (double) 1.0F, y + (double) 1.0F, z + (double) 1.0F);
                BoundingBox otherBox = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);

                boolean originalCollides = mask.intersectsWith(box);
                boolean otherCollides = mask.intersectsWith(otherBox);

                return SplitStateBoolean.result(originalCollides, otherCollides);
            }
        }

        BoundingBox box = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);

        if (mask.intersectsWith(box)) {
            return SplitStateBoolean.TRUE;
        }

        return SplitStateBoolean.FALSE;
    }

    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        float f = 0.0625F;
        float minX = 1.0F;
        float minY = 1.0F;
        float minZ = 1.0F;
        float maxX = 0.0F;
        float maxY = 0.0F;
        float maxZ = 0.0F;
        boolean flag = false;

        if (state.getWest() == West.TRUE) {
            maxX = 0.0625F;
            minX = 0.0F;
            minY = 0.0F;
            maxY = 1.0F;
            minZ = 0.0F;
            maxZ = 1.0F;
            flag = true;
        }

        if (state.getEast() == East.TRUE) {
            minX = Math.min(minX, 0.9375F);
            maxX = 1.0F;
            minY = 0.0F;
            maxY = 1.0F;
            minZ = 0.0F;
            maxZ = 1.0F;
            flag = true;
        }

        if (state.getNorth() == North.TRUE) {
            maxZ = Math.max(maxZ, 0.0625F);
            minZ = 0.0F;
            minX = 0.0F;
            maxX = 1.0F;
            minY = 0.0F;
            maxY = 1.0F;
            flag = true;
        }

        if (state.getSouth() == South.TRUE) {
            minZ = Math.min(minZ, 0.9375F);
            maxZ = 1.0F;
            minX = 0.0F;
            maxX = 1.0F;
            minY = 0.0F;
            maxY = 1.0F;
            flag = true;
        }

        if (!flag && this.canPlaceOn(data.getWorldTracker().getBlock(x, y + 1, z).getValue())) {
            minY = 0.9375F;
            maxY = 1.0F;
            minX = 0.0F;
            maxX = 1.0F;
            minZ = 0.0F;
            maxZ = 1.0F;
        }

        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public boolean isFullCube(WrappedBlockState state) {
        return false;
    }

    private SplitStateBoolean canPlaceOn(SplitState<WrappedBlockState> splitState) {
        boolean latest = canPlaceOn(splitState.getValue());
        Boolean old = splitState.getOldValue() != null ? canPlaceOn(splitState.getOldValue()) : null;

        if (old == null) {
            return latest ? SplitStateBoolean.TRUE : SplitStateBoolean.FALSE;
        }

        if (latest == old) {
            return latest ? SplitStateBoolean.TRUE : SplitStateBoolean.FALSE;
        } else {
            return SplitStateBoolean.POSSIBLE;
        }
    }

    private boolean canPlaceOn(WrappedBlockState state) {
        return Acid.get().getBlockManager().isFullBlock(state) && BlockUtil.isSolid(state.getType().getMaterialType());
    }

    @Override
    public void onPlace(PlayerData data, int x, int y, int z, BlockFace face, Vector3f hit, int meta, WrappedBlockState state) {
        if (BlockFaces.isHorizontal(face)) {
            switch (face) {
                case NORTH:
                    state.setSouth(South.TRUE);
                    break;
                case EAST:
                    state.setWest(West.TRUE);
                    break;
                case SOUTH:
                    state.setNorth(North.TRUE);
                    break;
                case WEST:
                    state.setEast(East.TRUE);
                    break;
            }
        }
    }
}
