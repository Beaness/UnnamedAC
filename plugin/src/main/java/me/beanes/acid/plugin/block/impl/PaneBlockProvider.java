package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.SplitState;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

import java.util.List;

public class PaneBlockProvider extends BlockProvider {
    @Override
    public void addPossibleCollisionBoxes(PlayerData data, int x, int y, int z, WrappedBlockState state, BoundingBox mask, List<BoundingBox> list) {
        StateType selfType = state.getType();

        SplitStateBoolean north = this.canConnectTo(data, x, y, z - 1, selfType);
        SplitStateBoolean south = this.canConnectTo(data, x, y, z + 1, selfType);
        SplitStateBoolean west = this.canConnectTo(data, x - 1, y, z, selfType);
        SplitStateBoolean east = this.canConnectTo(data, x + 1, y, z, selfType);

        if ((west.notPossible() || east.notPossible()) && (west.possible() || east.possible() || north.possible() || south.possible())) {
            if (west.possible()) {
                BoundingBox box = new BoundingBox(x, y, z + (double) 0.4375F, x + (double) 0.5F, y + (double) 1.0F, z + (double) 0.5625F);
                if (mask.intersectsWith(box)) {
                    list.add(box);
                }
            }

            if (east.possible()) {
                BoundingBox box = new BoundingBox(x + (double) 0.5F, y, z + (double) 0.4375F, x + (double) 1.0F, y + (double) 1.0F, z + (double) 0.5625F);
                if (mask.intersectsWith(box)) {
                    list.add(box);
                }
            }
        }

        if ((west.possible() && east.possible()) || (west.notPossible() && east.notPossible() && north.notPossible() && south.notPossible())) {
            BoundingBox box = new BoundingBox(x, y, z + (double) 0.4375F, x + (double) 1.0F, y + (double) 1.0F, z + (double) 0.5625F);
            if (mask.intersectsWith(box)) {
                list.add(box);
            }
        }

        if ((north.notPossible() || south.notPossible()) && (west.possible() || east.possible() || north.possible() || south.possible())) {
            if (north.possible()) {
                BoundingBox box = new BoundingBox(x + (double) 0.4375F, y, z, x + (double) 0.5625F, y + (double) 1.0F, z + (double) 0.5F);
                if (mask.intersectsWith(box)) {
                    list.add(box);
                }
            }

            if (south.possible()) {
                BoundingBox box = new BoundingBox(x + (double) 0.4375F, y, z + (double) 0.5F, x + (double) 0.5625F, y + (double) 1.0F, z + (double) 1.0F);
                if (mask.intersectsWith(box)) {
                    list.add(box);
                }
            }
        }

        if ((north.possible() && south.possible()) || (west.notPossible() && east.notPossible() && north.notPossible() && south.notPossible())) {
            BoundingBox box = new BoundingBox(x + (double) 0.4375F, y, z, x + (double) 0.5625F, y + (double) 1.0F, z + (double) 1.0F);
            if (mask.intersectsWith(box)) {
                list.add(box);
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

        boolean possibleCollision = false;

        if ((west.notPossible() || east.notPossible()) && (west.possible() || east.possible() || north.possible() || south.possible())) {
            boolean certain = (west == SplitStateBoolean.FALSE || east == SplitStateBoolean.FALSE) && (west == SplitStateBoolean.TRUE || east == SplitStateBoolean.TRUE || north == SplitStateBoolean.TRUE || south == SplitStateBoolean.TRUE);

            if (west.possible()) {
                BoundingBox box = new BoundingBox(x, y, z + (double) 0.4375F, x + (double) 0.5F, y + (double) 1.0F, z + (double) 0.5625F);
                if (mask.intersectsWith(box)) {
                    if (certain) {
                        return SplitStateBoolean.TRUE;
                    } else {
                        possibleCollision = true;
                    }
                }
            }

            if (east.possible()) {
                BoundingBox box = new BoundingBox(x + (double) 0.5F, y, z + (double) 0.4375F, x + (double)  1.0F, y + (double) 1.0F, z + (double) 0.5625F);
                if (mask.intersectsWith(box)) {
                    if (certain) {
                        return SplitStateBoolean.TRUE;
                    } else {
                        possibleCollision = true;
                    }
                }
            }
        }

        if ((west.possible() && east.possible()) || (west.notPossible() && east.notPossible() && north.notPossible() && south.notPossible())) {
            boolean certain = (west == SplitStateBoolean.TRUE && east == SplitStateBoolean.TRUE) || (west == SplitStateBoolean.FALSE && east == SplitStateBoolean.FALSE && north == SplitStateBoolean.FALSE && south == SplitStateBoolean.FALSE);

            BoundingBox box = new BoundingBox(x, y, z + (double) 0.4375F, x + (double) 1.0F, y + (double) 1.0F, z + (double) 0.5625F);
            if (mask.intersectsWith(box)) {
                if (certain) {
                    return SplitStateBoolean.TRUE;
                } else {
                    possibleCollision = true;
                }
            }
        }

        if ((north.notPossible() || south.notPossible()) && (west.possible() || east.possible() || north.possible() || south.possible())) {
            boolean certain = (north == SplitStateBoolean.FALSE || south == SplitStateBoolean.FALSE) && (west == SplitStateBoolean.TRUE || east == SplitStateBoolean.TRUE || north == SplitStateBoolean.TRUE || south == SplitStateBoolean.TRUE);

            if (north.possible()) {
                BoundingBox box = new BoundingBox(x + (double) 0.4375F, y, z, x + (double) 0.5625F, y + (double) 1.0F, z + (double) 0.5F);
                if (mask.intersectsWith(box)) {
                    if (certain) {
                        return SplitStateBoolean.TRUE;
                    } else {
                        possibleCollision = true;
                    }
                }
            }

            if (south.possible()) {
                BoundingBox box = new BoundingBox(x + (double) 0.4375F, y, z + (double) 0.5F, x + (double) 0.5625F, y + (double) 1.0F, z + (double) 1.0F);
                if (mask.intersectsWith(box)) {
                    if (certain) {
                        return SplitStateBoolean.TRUE;
                    } else {
                        possibleCollision = true;
                    }
                }
            }
        }

        if ((north.possible() && south.possible()) || (west.notPossible() && east.notPossible() && north.notPossible() && south.notPossible())) {
            boolean certain = (north == SplitStateBoolean.TRUE && south == SplitStateBoolean.TRUE) || (west == SplitStateBoolean.FALSE && east == SplitStateBoolean.FALSE && north == SplitStateBoolean.FALSE && south == SplitStateBoolean.FALSE);

            BoundingBox box = new BoundingBox(x + (double) 0.4375F, y, z, x + (double) 0.5625F, y + (double) 1.0F, z + (double) 1.0F);
            if (mask.intersectsWith(box)) {
                if (certain) {
                    return SplitStateBoolean.TRUE;
                } else {
                    possibleCollision = true;
                }
            }
        }

        return possibleCollision ? SplitStateBoolean.POSSIBLE : SplitStateBoolean.FALSE;
    }

    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        StateType selfType = state.getType();

        boolean north = this.canConnectTo(data, x, y, z - 1, selfType) == SplitStateBoolean.TRUE;
        boolean south = this.canConnectTo(data, x, y, z + 1, selfType) == SplitStateBoolean.TRUE;
        boolean west = this.canConnectTo(data, x - 1, y, z, selfType) == SplitStateBoolean.TRUE;
        boolean east = this.canConnectTo(data, x + 1, y, z, selfType) == SplitStateBoolean.TRUE;

        float minX = 0.4375F;
        float maxX = 0.5625F;
        float minZ = 0.4375F;
        float maxZ = 0.5625F;

        if ((!west || !east) && (west || east || north || south)) {
            if (west) {
                minX = 0.0F;
            } else if (east) {
                maxX = 1.0F;
            }
        } else {
            minX = 0.0F;
            maxX = 1.0F;
        }

        if ((!north || !south) && (west || east || north || south)) {
            if (north) {
                minZ = 0.0F;
            } else if (south) {
                maxZ = 1.0F;
            }
        } else {
            minZ = 0.0F;
            maxZ = 1.0F;
        }

        return new BoundingBox(minX, 0.0F, minZ, maxX, 1.0F, maxZ);
    }

    private SplitStateBoolean canConnectTo(PlayerData data, int x, int y, int z, StateType selfType) {
        SplitState<WrappedBlockState> splitState = data.getWorldTracker().getBlock(x, y, z);

        boolean latest = canConnectTo(splitState.getValue(), selfType);
        boolean old = splitState.getOldValue() != null ? canConnectTo(splitState.getOldValue(), selfType) : latest;

        return SplitStateBoolean.result(latest, old);
    }

    private boolean canConnectTo(WrappedBlockState state, StateType selfType) {
        if (state.getType().equals(selfType)) {
            return true;
        }

        if (BlockTags.GLASS_BLOCKS.contains(state.getType())) {
            return true;
        }

        if (BlockTags.GLASS_PANES.contains(state.getType())) {
            return true;
        }

        return Acid.get().getBlockManager().isFullBlock(state);
    }

    @Override
    public boolean isFullCube(WrappedBlockState state) {
        return false;
    }
}
