package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.world.MaterialType;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.SplitState;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

import java.util.List;

public class FenceBlockProvider extends BlockProvider {
    @Override
    public void addPossibleCollisionBoxes(PlayerData data, int x, int y, int z, WrappedBlockState state, BoundingBox mask, List<BoundingBox> list) {
        MaterialType materialType = state.getType().getMaterialType();

        SplitStateBoolean north = this.canConnectTo(data, x, y, z - 1, materialType);
        SplitStateBoolean south = this.canConnectTo(data, x, y, z + 1, materialType);
        SplitStateBoolean west = this.canConnectTo(data, x - 1, y, z, materialType);
        SplitStateBoolean east = this.canConnectTo(data, x + 1, y, z, materialType);

        float[] minZPossible;
        float[] maxZPossible;

        if (north == SplitStateBoolean.TRUE) {
            minZPossible = new float[]{0.0F};
        } else if (north == SplitStateBoolean.FALSE) {
            minZPossible = new float[]{0.375F};
        } else {
            minZPossible = new float[]{0.0F, 0.375F};
        }

        if (south == SplitStateBoolean.TRUE) {
            maxZPossible = new float[]{1.0F};
        } else if (south == SplitStateBoolean.FALSE) {
            maxZPossible = new float[]{0.625F};
        } else {
            maxZPossible = new float[]{1.0F, 0.625F};

        }

        if (north.possible() || south.possible()) {
            for (float minZ : minZPossible) {
                for (float maxZ : maxZPossible) {
                    BoundingBox box = new BoundingBox(x + 0.375F, y, z + minZ, x + 0.625F, y + 1.5F, z + maxZ);

                    if (mask.intersectsWith(box)) {
                        list.add(box);
                    }
                }
            }
        }


        float[] minXPossible;
        float[] maxXPossible;

        if (west == SplitStateBoolean.TRUE) {
            minXPossible = new float[]{0.0F};
        } else if (west == SplitStateBoolean.FALSE) {
            minXPossible = new float[]{0.375F};
        } else {
            minXPossible = new float[]{0.0F, 0.375F};
        }

        if (east == SplitStateBoolean.TRUE) {
            maxXPossible = new float[]{1.0F};
        } else if (east == SplitStateBoolean.FALSE) {
            maxXPossible = new float[]{0.625F};
        } else {
            maxXPossible = new float[]{1.0F, 0.625F};
        }

        if (west.possible() || east.possible() || north.notPossible() && south.notPossible()) {
            for (float minX : minXPossible) {
                for (float maxX : maxXPossible) {
                    BoundingBox box = new BoundingBox(x + minX, y, z + 0.375F, x + maxX, y + 1.5F, z + 0.625F);

                    if (mask.intersectsWith(box)) {
                        list.add(box);
                    }
                }
            }
        }
    }

    @Override
    public SplitStateBoolean isColliding(PlayerData data, int x, int y, int z, WrappedBlockState state, BoundingBox mask) {
        MaterialType materialType = state.getType().getMaterialType();

        SplitStateBoolean north = this.canConnectTo(data, x, y, z - 1, materialType);
        SplitStateBoolean south = this.canConnectTo(data, x, y, z + 1, materialType);
        SplitStateBoolean west = this.canConnectTo(data, x - 1, y, z, materialType);
        SplitStateBoolean east = this.canConnectTo(data, x + 1, y, z, materialType);

        float[] minZPossible;
        float[] maxZPossible;

        if (north == SplitStateBoolean.TRUE) {
            minZPossible = new float[]{0.0F};
        } else if (north == SplitStateBoolean.FALSE) {
            minZPossible = new float[]{0.375F};
        } else {
            minZPossible = new float[]{0.0F, 0.375F};
        }

        if (south == SplitStateBoolean.TRUE) {
            maxZPossible = new float[]{1.0F};
        } else if (south == SplitStateBoolean.FALSE) {
            maxZPossible = new float[]{0.625F};
        } else {
            maxZPossible = new float[]{1.0F, 0.625F};
        }

        boolean possibleCollision = false;

        if (north.possible() || south.possible()) {
            boolean certain = (north == SplitStateBoolean.TRUE && south == SplitStateBoolean.TRUE);
            for (float minZ : minZPossible) {
                for (float maxZ : maxZPossible) {
                    BoundingBox box = new BoundingBox(x + 0.375F, y, z + minZ, x + 0.625F, y + 1.5F, z + maxZ);

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


        float[] minXPossible;
        float[] maxXPossible;

        if (west == SplitStateBoolean.TRUE) {
            minXPossible = new float[]{0.0F};
        } else if (west == SplitStateBoolean.FALSE) {
            minXPossible = new float[]{0.375F};
        } else {
            minXPossible = new float[]{0.0F, 0.375F};
        }

        if (east == SplitStateBoolean.TRUE) {
            maxXPossible = new float[]{1.0F};
        } else if (east == SplitStateBoolean.FALSE) {
            maxXPossible = new float[]{0.625F};
        } else {
            maxXPossible = new float[]{1.0F, 0.625F};
        }

        if (west.possible() || east.possible() || (north.notPossible() && south.notPossible())) {
            boolean certain = (west == SplitStateBoolean.TRUE && east == SplitStateBoolean.TRUE) || (north == SplitStateBoolean.FALSE && south == SplitStateBoolean.FALSE);
            for (float minX : minXPossible) {
                for (float maxX : maxXPossible) {
                    BoundingBox box = new BoundingBox(x + minX, y, z + 0.375F, x + maxX, y + 1.5F, z + 0.625F);

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

        return possibleCollision ? SplitStateBoolean.POSSIBLE : SplitStateBoolean.FALSE;
    }

    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        MaterialType materialType = state.getType().getMaterialType();

        // Prioritize true states
        boolean north = this.canConnectTo(data, x, y, z - 1, materialType) == SplitStateBoolean.TRUE;
        boolean south = this.canConnectTo(data, x, y, z + 1, materialType) == SplitStateBoolean.TRUE;
        boolean west = this.canConnectTo(data, x - 1, y, z, materialType) == SplitStateBoolean.TRUE;
        boolean east = this.canConnectTo(data, x + 1, y, z, materialType) == SplitStateBoolean.TRUE;

        float minX = 0.375F;
        float maxX = 0.625F;
        float minZ = 0.375F;
        float maxZ = 0.625F;

        if (north) {
            minZ = 0.0F;
        }

        if (south) {
            maxZ = 1.0F;
        }

        if (west) {
            minX = 0.0F;
        }

        if (east) {
            maxX = 1.0F;
        }

        return new BoundingBox(minX, 0.0F, minZ, maxX, 1.0F, maxZ);
    }

    private SplitStateBoolean canConnectTo(PlayerData data, int x, int y, int z, MaterialType selfMaterial) {
        SplitState<WrappedBlockState> splitState = data.getWorldTracker().getBlock(x, y, z);

        boolean latest = canConnectTo(splitState.getValue(), selfMaterial);
        boolean old = splitState.getOldValue() != null ? canConnectTo(splitState.getOldValue(), selfMaterial) : latest;

        return SplitStateBoolean.result(latest, old);
    }

    private boolean canConnectTo(WrappedBlockState state, MaterialType selfMaterial) {
        if (state.getType() == StateTypes.BARRIER) {
            return false;
        }

        if (state.getType().getMaterialType() == MaterialType.VEGETABLE) {
            return false;
        }

        if ((state.getType() == StateTypes.OAK_FENCE || state.getType() == StateTypes.NETHER_BRICK_FENCE) && state.getType().getMaterialType() == selfMaterial) {
            return true;
        }

        if (state.getType() == StateTypes.OAK_FENCE_GATE) {
            return true;
        }

        return state.getType().isSolid() && Acid.get().getBlockManager().isFullBlock(state);
    }

    @Override
    public SplitStateBoolean onActivate(PlayerData data, int x, int y, int z, WrappedBlockState state, boolean latest) {
        return SplitStateBoolean.TRUE;
    }

    @Override
    public boolean isFullCube(WrappedBlockState state) {
        return false;
    }
}
