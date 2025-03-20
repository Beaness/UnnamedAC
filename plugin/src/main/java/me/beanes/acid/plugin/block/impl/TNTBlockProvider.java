package me.beanes.acid.plugin.block.impl;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import me.beanes.acid.plugin.block.BlockProvider;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.util.splitstate.ConfirmableState;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

public class TNTBlockProvider extends BlockProvider {
    @Override
    public SplitStateBoolean onActivate(PlayerData data, int x, int y, int z, WrappedBlockState state, boolean latest) {
        ConfirmableState<ItemStack> heldItem = data.getInventoryTracker().getHeldItem();

        boolean last = canBeUsed(heldItem.getValue().getType());
        boolean old = heldItem.getOldValue() != null ? canBeUsed(heldItem.getOldValue().getType()) : last;

        SplitStateBoolean result = SplitStateBoolean.result(last, old);

        if (result.possible()) {
            if (latest) {
                data.getWorldTracker().setBlock(x, y, z, WrappedBlockState.getDefaultState(StateTypes.AIR));
            } else {
                data.getWorldTracker().setOldBlock(x, y, z, WrappedBlockState.getDefaultState(StateTypes.AIR));
            }
        }

        if (result == SplitStateBoolean.POSSIBLE) {
            data.getWorldTracker().getResyncHandler().scheduleResync(x, y, z);
        }

        return SplitStateBoolean.result(last, old);
    }

    private boolean canBeUsed(ItemType type) {
        return type == ItemTypes.FLINT_AND_STEEL || type == ItemTypes.FIRE_CHARGE;
    }
}
