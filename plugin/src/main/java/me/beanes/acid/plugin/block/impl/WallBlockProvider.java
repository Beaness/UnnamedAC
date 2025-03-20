package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.MaterialType;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.SplitState;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

import java.util.List;

public class WallBlockProvider extends BlockProvider {
    @Override
    public void addPossibleCollisionBoxes(PlayerData data, int x, int y, int z, WrappedBlockState state, BoundingBox mask, List<BoundingBox> list) {
        StateType selfType = state.getType();

        SplitStateBoolean north = this.canConnectTo(data, x, y, z - 1, selfType);
        SplitStateBoolean south = this.canConnectTo(data, x, y, z + 1, selfType);
        SplitStateBoolean west = this.canConnectTo(data, x - 1, y, z, selfType);
        SplitStateBoolean east = this.canConnectTo(data, x + 1, y, z, selfType);

        float[] minXPossible;
        float[] maxXPossible;
        float[] minZPossible;
        float[] maxZPossible;

        if (north == SplitStateBoolean.TRUE) {
            minZPossible = new float[]{0.0F};
        } else if (north == SplitStateBoolean.FALSE) {
            minZPossible = new float[]{0.25F};
        } else {
            minZPossible = new float[]{0.0F, 0.25F};
        }

        if (south == SplitStateBoolean.TRUE) {
            maxZPossible = new float[]{1.0F};
        } else if (south == SplitStateBoolean.FALSE) {
            maxZPossible = new float[]{0.75F};
        } else {
            maxZPossible = new float[]{1.0F, 0.75F};
        }

        if (west == SplitStateBoolean.TRUE) {
            minXPossible = new float[]{0.0F};
        } else if (west == SplitStateBoolean.FALSE) {
            minXPossible = new float[]{0.25F};
        } else {
            minXPossible = new float[]{0.0F, 0.25F};
        }

        if (east == SplitStateBoolean.TRUE) {
            maxXPossible = new float[]{1.0F};
        } else if (east == SplitStateBoolean.FALSE) {
            maxXPossible = new float[]{0.75F};
        } else {
            maxXPossible = new float[]{1.0F, 0.75F};
        }

        if (north.possible() && south.possible() && west.notPossible() && east.notPossible()) {
            for (float minZ : minZPossible) {
                for (float maxZ : maxZPossible) {
                    BoundingBox box = new BoundingBox(x + (double) 0.3125F, y, z + (double) minZ, x + (double) 0.6875F, y + 1.5D, z + (double) maxZ);

                    if (mask.intersectsWith(box)) {
                        list.add(box);
                    }
                }
            }
        }

        if (north.notPossible() && south.notPossible() && west.possible() && east.possible()) {
            for (float minX : minXPossible) {
                for (float maxX : maxXPossible) {
                    BoundingBox box = new BoundingBox(x + (double) minX, y, z + (double) 0.3125F, x + (double) maxX, y + 1.5D, z + (double) 0.6875F);

                    if (mask.intersectsWith(box)) {
                        list.add(box);
                    }
                }
            }
        }

        for (float minX : minXPossible) {
            for (float maxX : maxXPossible) {
                for (float minZ : minZPossible) {
                    for (float maxZ : maxZPossible) {
                        BoundingBox box = new BoundingBox(x + minX, y, z + minZ, x + maxX, y + 1.5D, z + maxZ);

                        if (mask.intersectsWith(box)) {
                            list.add(box);
                        }
                    }
                }
            }
        }
    }

    @Override
    public SplitStateBoolean isColliding(PlayerData data, int x, int y, int z, WrappedBlockState state, BoundingBox mask) {
        StateType selfType = state.getType();

        SplitStateBoolean north = this.canConnectTo(data, x, y, z - 1, selfType);
        SplitStateBoolean south = this.canConnectTo(data, x, y, z + 1, selfType);
        SplitStateBoolean west = this.canConnectTo(data, x - 1, y, z, selfType);
        SplitStateBoolean east = this.canConnectTo(data, x + 1, y, z, selfType);

        float[] minXPossible;
        float[] maxXPossible;
        float[] minZPossible;
        float[] maxZPossible;

        if (north == SplitStateBoolean.TRUE) {
            minZPossible = new float[]{0.0F};
        } else if (north == SplitStateBoolean.FALSE) {
            minZPossible = new float[]{0.25F};
        } else {
            minZPossible = new float[]{0.0F, 0.25F};
        }

        if (south == SplitStateBoolean.TRUE) {
            maxZPossible = new float[]{1.0F};
        } else if (south == SplitStateBoolean.FALSE) {
            maxZPossible = new float[]{0.75F};
        } else {
            maxZPossible = new float[]{1.0F, 0.75F};
        }

        if (west == SplitStateBoolean.TRUE) {
            minXPossible = new float[]{0.0F};
        } else if (west == SplitStateBoolean.FALSE) {
            minXPossible = new float[]{0.25F};
        } else {
            minXPossible = new float[]{0.0F, 0.25F};
        }

        if (east == SplitStateBoolean.TRUE) {
            maxXPossible = new float[]{1.0F};
        } else if (east == SplitStateBoolean.FALSE) {
            maxXPossible = new float[]{0.75F};
        } else {
            maxXPossible = new float[]{1.0F, 0.75F};
        }

        boolean possibleCollision = false;


        if (north.possible() && south.possible() && west.notPossible() && east.notPossible()) {
            boolean certain = north == SplitStateBoolean.TRUE && south == SplitStateBoolean.TRUE && west == SplitStateBoolean.FALSE && east == SplitStateBoolean.FALSE;

            for (float minZ : minZPossible) {
                for (float maxZ : maxZPossible) {
                    BoundingBox box = new BoundingBox(x + 0.3125F, y, z + minZ, x + 0.6875F, y + 1.5D, z + maxZ);

                    if (mask.intersectsWith(box)) {
                        if (certain) {
                            return SplitStateBoolean.TRUE;
                        } else {
                            possibleCollision = true;
                        }
                    }
                }
            }
        }

        if (north.notPossible() && south.notPossible() && west.possible() && east.possible()) {
            boolean certain = north == SplitStateBoolean.FALSE && south == SplitStateBoolean.FALSE && west == SplitStateBoolean.TRUE && east == SplitStateBoolean.TRUE;

            for (float minX : minXPossible) {
                for (float maxX : maxXPossible) {
                    BoundingBox box = new BoundingBox(x + minX, y, z + 0.3125F, x + maxX, y + 1.5D, z + 0.6875F);

                    if (mask.intersectsWith(box)) {
                        if (certain) {
                            return SplitStateBoolean.TRUE;
                        } else {
                            possibleCollision = true;
                        }
                    }
                }
            }
        }

        boolean certain = minXPossible.length == 1 && maxXPossible.length == 1 && minZPossible.length == 1 && maxZPossible.length == 1;

        for (float minX : minXPossible) {
            for (float maxX : maxXPossible) {
                for (float minZ : minZPossible) {
                    for (float maxZ : maxZPossible) {
                        BoundingBox box = new BoundingBox(x + minX, y, z + minZ, x + maxX, y + 1.5D, z + maxZ);

                        if (mask.intersectsWith(box)) {
                            if (certain) {
                                return SplitStateBoolean.TRUE;
                            } else {
                                possibleCollision = true;
                            }
                        }
                    }
                }
            }
        }

        return possibleCollision ? SplitStateBoolean.TRUE : SplitStateBoolean.FALSE;
    }

    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        StateType selfType = state.getType();

        boolean north = this.canConnectTo(data, x, y, z - 1, selfType) == SplitStateBoolean.TRUE;
        boolean south = this.canConnectTo(data, x, y, z + 1, selfType) == SplitStateBoolean.TRUE;
        boolean west = this.canConnectTo(data, x - 1, y, z, selfType) == SplitStateBoolean.TRUE;
        boolean east = this.canConnectTo(data, x + 1, y, z, selfType) == SplitStateBoolean.TRUE;

        float minX = 0.25F;
        float maxX = 0.75F;
        float minZ = 0.25F;
        float maxZ = 0.75F;
        float maxY = 1.0F;

        if (north)
        {
            minZ = 0.0F;
        }

        if (south)
        {
            maxZ = 1.0F;
        }

        if (west)
        {
            minX = 0.0F;
        }

        if (east)
        {
            maxX = 1.0F;
        }

        if (north && south && !west && !east)
        {
            maxY = 0.8125F;
            minX = 0.3125F;
            maxX = 0.6875F;
        }
        else if (!north && !south && west && east)
        {
            maxY = 0.8125F;
            minZ = 0.3125F;
            maxZ = 0.6875F;
        }

        return new BoundingBox(minX, 0.0F, minZ, maxX, maxY, maxZ);
    }

    @Override
    public boolean isFullCube(WrappedBlockState state) {
        return false;
    }

    private SplitStateBoolean canConnectTo(PlayerData data, int x, int y, int z, StateType selfType) {
        SplitState<WrappedBlockState> splitState = data.getWorldTracker().getBlock(x, y, z);

        boolean latest = canConnectTo(splitState.getValue(), selfType);
        boolean old = splitState.getOldValue() != null ? canConnectTo(splitState.getOldValue(), selfType) : latest;

        return SplitStateBoolean.result(latest, old);
    }

    private boolean canConnectTo(WrappedBlockState state, StateType selfType) {
        if (state.getType() == StateTypes.BARRIER) {
            return false;
        }

        if (state.getType().getMaterialType() == MaterialType.VEGETABLE) {
            return false;
        }

        if (state.getType().equals(selfType)) {
            return true;
        }

        if (BlockTags.FENCES.contains(state.getType())) {
            return true;
        }

        if (state.getType() == StateTypes.OAK_FENCE_GATE) {
            return true;
        }

        return state.getType().isSolid() && Acid.get().getBlockManager().isFullBlock(state);
    }
}
