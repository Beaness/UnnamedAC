package me.beanes.acid.plugin.player.tracker.impl;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientHeldItemChange;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import lombok.Getter;
import lombok.Setter;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.player.tracker.Tracker;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;
import org.bukkit.Bukkit;

public class UsingTracker extends Tracker {

    public UsingTracker(PlayerData data) {
        super(data);
    }

    @Getter
    private SplitStateBoolean reducedByAttacking;

    @Getter @Setter
    private SplitStateBoolean using = SplitStateBoolean.FALSE;

    public void handleInteractEntity(WrapperPlayClientInteractEntity wrapper) {
        if (wrapper.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
            if (data.getEntityTracker().isTracking(wrapper.getEntityId())) {
                if (data.getActionTracker().isPossibleOverride()) {
                    SplitStateBoolean clientSprint = data.getActionTracker().getClientSprintMetadata();
                    boolean serverSprint = data.getActionTracker().isServerSprintState();

                    if (clientSprint == SplitStateBoolean.POSSIBLE) {
                        this.reducedByAttacking = SplitStateBoolean.POSSIBLE;
                    } else {
                        boolean clientSprintCertain = clientSprint == SplitStateBoolean.TRUE;

                        this.reducedByAttacking = SplitStateBoolean.result(clientSprintCertain, serverSprint);
                    }
                } else {
                    this.reducedByAttacking = data.getActionTracker().getClientSprintMetadata();
                }
            }
        }
    }

    public void handleBlockPlace(WrapperPlayClientPlayerBlockPlacement wrapper) {
        if (wrapper.getFace() != BlockFace.OTHER) {
            return;
        }

        if (!wrapper.getItemStack().isPresent()) {
            return;
        }

        ItemStack item = wrapper.getItemStack().get();
        ItemType type = item.getType();

        if (type.hasAttribute(ItemTypes.ItemAttribute.SWORD)) {
            this.using = SplitStateBoolean.TRUE;
            return;
        } else if (type.hasAttribute(ItemTypes.ItemAttribute.EDIBLE)) {
            // Splashable potions can't be drunk
            if (type == ItemTypes.POTION && (item.getLegacyData() & 16384) != 0) {
                return;
            }

            if (type == ItemTypes.GOLDEN_APPLE || type == ItemTypes.ENCHANTED_GOLDEN_APPLE || type == ItemTypes.POTION || type == ItemTypes.MILK_BUCKET) {
                this.using = SplitStateBoolean.TRUE;
                return;
            }

            int latestFood = data.getStateTracker().getFood().getValue();
            Integer oldFood = data.getStateTracker().getFood().getOldValue();

            // Certain state
            if (latestFood < 20 && oldFood == null) {
                this.using = SplitStateBoolean.TRUE;
                return;
            }

            // Uncertain state
            if (latestFood == 20 && oldFood != null && oldFood < 20) {
                this.using = SplitStateBoolean.POSSIBLE;
                return;
            }

            // Uncertain state
            if (latestFood < 20 && oldFood == 20) {
                this.using = SplitStateBoolean.POSSIBLE;
            }
        } else if (type == ItemTypes.BOW) {
            this.using = SplitStateBoolean.POSSIBLE; // If no arrows the player doesnt use it :P
        }
    }

    public void handleDigging(WrapperPlayClientPlayerDigging wrapper) {
        if (wrapper.getAction() == DiggingAction.RELEASE_USE_ITEM) {
            this.using = SplitStateBoolean.FALSE;
        }
    }

    // This is handled before inventory tracker
    public void handleHeldItemChange(WrapperPlayClientHeldItemChange wrapper) {
        Bukkit.broadcastMessage("HELD ITEM CHANGE!!");
        this.using = SplitStateBoolean.FALSE;
    }

    public void handleSetSlot(WrapperPlayServerSetSlot wrapper) {
        if (wrapper.getWindowId() == 0) {
            data.getTransactionTracker().pre(() -> {
                if (wrapper.getSlot() == (data.getInventoryTracker().getHeldSlot() + 36)) {
                    if (this.using.possible()) {
                        this.using = SplitStateBoolean.POSSIBLE;
                    }
                }
            });

            data.getTransactionTracker().post(() -> {
                if (wrapper.getSlot() == (data.getInventoryTracker().getHeldSlot() + 36)) {
                    this.using = SplitStateBoolean.FALSE;
                }
            });
        }
    }

    // TODO: implement window items
    public void handleWindowItems() {

    }

    public void handleEndClientTick() {
        this.reducedByAttacking = SplitStateBoolean.FALSE;

        // Set using to POSSIBLE after first simulation because we could have exited every tick
        if (data.getUsingTracker().getUsing() == SplitStateBoolean.TRUE) {
            data.getUsingTracker().setUsing(SplitStateBoolean.POSSIBLE);
        }
    }
}
