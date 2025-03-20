package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.ConfirmableState;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

public class FlowerPotBlockProvider extends BlockProvider {
    @Override
    public BoundingBox getCollisionBoundingBox(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        float f = 0.375F;
        float f1 = f / 2.0F;

        return new BoundingBox(x + (double) (0.5F - f1), y, z + (double) (0.5F - f1), x + (double) (0.5F + f1), y + (double) f, z + (double) (0.5F + f1));
    }

    @Override
    public boolean isFullCube(WrappedBlockState state) {
        return false;
    }

    @Override
    public SplitStateBoolean onActivate(PlayerData data, int x, int y, int z, WrappedBlockState state, boolean latest) {
        if (state.getType() != StateTypes.FLOWER_POT) {
            return SplitStateBoolean.FALSE;
        }

        ConfirmableState<ItemStack> heldItem = data.getInventoryTracker().getHeldItem();
        boolean last = canContain(heldItem.getValue());
        boolean old = heldItem.getOldValue() != null ? canContain(heldItem.getOldValue()) : last;

        SplitStateBoolean result = SplitStateBoolean.result(last, old);

        // 100% certain the flower pot can't contain the held item
        if (result == SplitStateBoolean.FALSE) {
            return SplitStateBoolean.FALSE;
        }

        SplitStateBoolean creative = data.getAbilitiesTracker().getCreativeMode();

        if (creative == SplitStateBoolean.FALSE) {
            if (last) {
                heldItem.getValue().setAmount(heldItem.getValue().getAmount() - 1);

                if (heldItem.getValue().getAmount() <= 0) {
                    heldItem.setValueImmediate(ItemStack.EMPTY);
                }
            }

            if (heldItem.getOldValue() != null && old) {
                heldItem.getOldValue().setAmount(heldItem.getOldValue().getAmount() - 1);

                if (heldItem.getOldValue().getAmount() <= 0) {
                    heldItem.setOldValueImmediate(ItemStack.EMPTY);
                }
            }
        }

        if (result.possible() && creative == SplitStateBoolean.POSSIBLE) {
            data.getInventoryTracker().attemptResync();
        }


        if (result == SplitStateBoolean.POSSIBLE) {
            data.getWorldTracker().getResyncHandler().scheduleResync(x, y, z);
            data.getInventoryTracker().attemptResync();
        }

        return result;
    }

    private boolean canContain(ItemStack stack) {
        return stack != ItemStack.EMPTY && canContain(stack.getType().getPlacedType());
    }

    private boolean canContain(StateType type) {
        return BlockTags.SMALL_FLOWERS.contains(type)
                || BlockTags.SAPLINGS.contains(type)
                || type == StateTypes.CACTUS
                || type == StateTypes.BROWN_MUSHROOM
                || type == StateTypes.RED_MUSHROOM
                || type == StateTypes.DEAD_BUSH
                || type == StateTypes.FERN;
    }
}
