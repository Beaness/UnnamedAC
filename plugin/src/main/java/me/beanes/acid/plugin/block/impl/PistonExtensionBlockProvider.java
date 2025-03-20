package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

import java.util.List;

public class PistonExtensionBlockProvider extends BlockProvider {
    @Override
    public void addPossibleCollisionBoxes(PlayerData data, int x, int y, int z, WrappedBlockState state, BoundingBox mask, List<BoundingBox> list) {
        BlockFace facing = state.getFacing();

        if (facing != null) {
            BoundingBox head;
            BoundingBox core;

            switch (facing) {
                case DOWN:
                    head = new BoundingBox(x, y, z, x + (double) 1.0F, y + (double) 0.25F, z + (double) 1.0F);
                    core = new BoundingBox(x + (double) 0.375F, y + (double) 0.25F, z + (double) 0.375F, x + (double) 0.625F, y + (double) 1.0F, z + (double) 0.625F);
                    break;

                case UP:
                    head = new BoundingBox(x, y + (double) 0.75F, z, x + (double) 1.0F, y + (double) 1.0F, z + (double) 1.0F);
                    core = new BoundingBox(x + (double) 0.375F, y, z + (double) 0.375F, x + (double) 0.625F, y + (double) 0.75F, z + (double) 0.625F);
                    break;

                case NORTH:
                    head = new BoundingBox(x, y, z, x + (double) 1.0F, y + (double) 1.0F, z + (double) 0.25F);
                    core = new BoundingBox(x + (double) 0.25F, y + (double) 0.375F, z + (double) 0.25F, x + (double) 0.75F, y + (double) 0.625F, z + (double) 1.0F);
                    break;

                case SOUTH:
                    head = new BoundingBox(x, y, z + (double) 0.75F, x + (double) 1.0F, y + (double) 1.0F, z + (double) 1.0F);
                    core = new BoundingBox(x + (double) 0.25F, y + (double) 0.375F, z, x + (double) 0.75F, y + (double) 0.625F, z + (double) 0.75F);
                    break;

                case WEST:
                    head = new BoundingBox(x, y, z, x + (double) 0.25F, y + (double) 1.0F, z + (double) 1.0F);
                    core = new BoundingBox(x + (double) 0.375F, y + (double) 0.25F, z + (double) 0.25F, x + (double) 0.625F, y + (double) 0.75F, z + (double) 1.0F);
                    break;

                case EAST:
                    head = new BoundingBox(x + (double) 0.75F, y, z, x + (double) 1.0F, y + (double) 1.0F, z + (double) 1.0F);
                    core = new BoundingBox(x, y + (double) 0.375F, z + (double) 0.25F, x + (double) 0.75F, y + (double) 0.625F, z + (double) 0.75F);
                    break;

                default:
                    super.addPossibleCollisionBoxes(data, x, y, z, state, mask, list);
                    return;
            }

            if (mask.intersectsWith(head)) {
                list.add(head);
            }

            if (mask.intersectsWith(core)) {
                list.add(core);
            }
        }
    }

    @Override
    public SplitStateBoolean isColliding(PlayerData data, int x, int y, int z, WrappedBlockState state, BoundingBox mask) {
        BlockFace facing = state.getFacing();

        if (facing != null) {
            BoundingBox head;
            BoundingBox core;

            switch (facing) {
                case DOWN:
                    head = new BoundingBox(x, y, z, x + (double) 1.0F, y + (double) 0.25F, z + (double) 1.0F);
                    core = new BoundingBox(x + (double) 0.375F, y + (double) 0.25F, z + (double) 0.375F, x + (double) 0.625F, y + (double) 1.0F, z + (double) 0.625F);
                    break;

                case UP:
                    head = new BoundingBox(x, y + (double) 0.75F, z, x + (double) 1.0F, y + (double) 1.0F, z + (double) 1.0F);
                    core = new BoundingBox(x + (double) 0.375F, y, z + (double) 0.375F, x + (double) 0.625F, y + (double) 0.75F, z + (double) 0.625F);
                    break;

                case NORTH:
                    head = new BoundingBox(x, y, z, x + (double) 1.0F, y + (double) 1.0F, z + (double) 0.25F);
                    core = new BoundingBox(x + (double) 0.25F, y + (double) 0.375F, z + (double) 0.25F, x + (double) 0.75F, y + (double) 0.625F, (double) z + 1.0F);
                    break;

                case SOUTH:
                    head = new BoundingBox(x, y, z + (double) 0.75F, x + (double) 1.0F, y + (double) 1.0F, z + (double) 1.0F);
                    core = new BoundingBox(x + (double) 0.25F, y + (double) 0.375F, z, x + (double) 0.75F, y + (double) 0.625F, z + (double) 0.75F);
                    break;

                case WEST:
                    head = new BoundingBox(x, y, z, x + (double) 0.25F, y + (double) 1.0F, z + (double) 1.0F);
                    core = new BoundingBox(x + (double) 0.375F, y + (double) 0.25F, z + (double) 0.25F, x + (double) 0.625F, y + (double) 0.75F, z + (double) 1.0F);
                    break;

                case EAST:
                    head = new BoundingBox(x + (double) 0.75F, y, z, x + (double) 1.0F, y + (double) 1.0F, z + (double) 1.0F);
                    core = new BoundingBox(x, y + (double) 0.375F, z + (double) 0.25F, x + (double) 0.75F, y + (double) 0.625F, z + (double) 0.75F);
                    break;

                default:
                    return super.isColliding(data, x, y, z, state, mask);
            }

            if (mask.intersectsWith(head)) {
                return SplitStateBoolean.TRUE;
            }

            if (mask.intersectsWith(core)) {
                return SplitStateBoolean.TRUE;
            }
        }

        return SplitStateBoolean.FALSE;
    }

    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        BlockFace facing = state.getFacing();

        switch (facing)
        {
            case DOWN:
                return new BoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 0.25F, 1.0F);

            case UP:
                return new BoundingBox(0.0F, 0.75F, 0.0F, 1.0F, 1.0F, 1.0F);

            case NORTH:
                return new BoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.25F);

            case SOUTH:
                return new BoundingBox(0.0F, 0.0F, 0.75F, 1.0F, 1.0F, 1.0F);

            case WEST:
                return new BoundingBox(0.0F, 0.0F, 0.0F, 0.25F, 1.0F, 1.0F);

            case EAST:
            default:
                return new BoundingBox(0.75F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    public boolean isFullCube(WrappedBlockState state) {
        return false;
    }
}
