package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerAbilities;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.ConfirmableState;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

public class CakeBlockProvider extends BlockProvider {
    @Override
    public BoundingBox getCollisionBoundingBox(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        float f = 0.0625F;
        float f1 = (float)(1 + state.getBites() * 2) / 16.0F;
        float f2 = 0.5F;

        return new BoundingBox((float)x + f1, y, (float)z + f, (float)(x + 1) - f, (float)y + f2, (float)(z + 1) - f);
    }

    @Override
    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        float f = 0.0625F;
        float f1 = (float)(1 + state.getBites() * 2) / 16.0F;
        float f2 = 0.5F;

        return new BoundingBox(f1, 0.0F, f, 1.0F - f, f2, 1.0F - f);
    }

    @Override
    public boolean isFullCube(WrappedBlockState state) {
        return false;
    }

    @Override
    public SplitStateBoolean onActivate(PlayerData data, int x, int y, int z, WrappedBlockState state, boolean latest) {
        SplitStateBoolean canEat = canEat(data);

        if (canEat.possible()) {
            if (state.getBites() < 6) {
                state.setBites(state.getBites() + 1);

                if (latest) {
                    data.getWorldTracker().setBlock(x, y, z, state);
                } else {
                    data.getWorldTracker().setOldBlock(x, y, z, state);
                }
            } else {
                if (latest) {
                    data.getWorldTracker().setBlock(x, y, z, WrappedBlockState.getByGlobalId(0));
                } else {
                    data.getWorldTracker().setOldBlock(x, y, z, WrappedBlockState.getByGlobalId(0));
                }
            }

            // We don't know if the player ate the block :(
            if (canEat == SplitStateBoolean.POSSIBLE) {
                data.getWorldTracker().getResyncHandler().scheduleResync(x, y, z);
            }
        }

        return canEat;
    }

    private SplitStateBoolean canEat(PlayerData data) {
        SplitStateBoolean godMode = data.getAbilitiesTracker().getGodMode();

        // Can't eat if 100% certain in god mode -> can't eat
        if (godMode == SplitStateBoolean.FALSE) {
            return SplitStateBoolean.FALSE;
        }

        ConfirmableState<Integer> foodLevel = data.getStateTracker().getFood();

        boolean canEatLatest = foodLevel.getValue() < 20;
        boolean canEatOld = foodLevel.getOldValue() != null ? foodLevel.getOldValue() < 20 : canEatLatest;

        SplitStateBoolean eatResult = SplitStateBoolean.result(canEatLatest, canEatOld);

        // 100% certain the player does not have enough food
        if (eatResult == SplitStateBoolean.FALSE) {
            return SplitStateBoolean.FALSE;
        }

        return SplitStateBoolean.result(godMode, eatResult);
    }
}
