package me.beanes.acid.plugin.player.tracker.impl;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.wrapper.play.client.*;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import lombok.Getter;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.player.tracker.Tracker;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

import java.util.List;

public class ActionTracker extends Tracker {
    public ActionTracker(PlayerData data) {
        super(data);
    }

    @Getter
    private boolean sneaking; // This can technically be desynced on join but hey.. dont make my life too hard :P

    // The metadata can desync because of serverSprintState not resetting when switching servers
    // It should be fine after one start sprint or stop sprint packet has been sent
    // Even if the server overrides the client sprint metadata, the client will tell us it has been updated
    @Getter
    private SplitStateBoolean clientSprintMetadata = SplitStateBoolean.POSSIBLE;
    @Getter
    private SplitStateBoolean lastClientSprintMetadata = SplitStateBoolean.POSSIBLE;


    // The following dumb code is made for minecraft which sends out a sprint update on the entity data
    // This can cause 2 issues:
    //    - Client slowdown of hitting isn't applied (the server override gets processed before the client sprint key gets processed)
    //    - Omnisprint, the player can move backwards without the sprinting modifier from the attribute is removed, because if the server sets sprinting to false the modifier will never be removed (as it only checks to remove the modifier if sprinting is true)
    @Getter
    private boolean possibleOverride = false;
    @Getter
    private boolean serverSprintState = false;
    private boolean resetOverride = false;
    @Getter
    private boolean uncertainSprintAttribute = false;
    private int lastSprintOverridePreTransaction = 0;
    // This is used to store a bitmask of useful metadata, if a server metadata update is useless (like updating sneaking/sprinting the anticheat will cancel it)
    private int lastUsefulMetadata = 0;

    public void handleEntityAction(WrapperPlayClientEntityAction wrapper) {
        switch (wrapper.getAction()) {
            case START_SPRINTING:
                System.out.println("[!] Sprint true");
                clientSprintMetadata = SplitStateBoolean.TRUE;
                if (!possibleOverride) { // The client updated their sprint which was 100% not triggered by the server
                    uncertainSprintAttribute = false;
                }
                break;
            case STOP_SPRINTING:
                System.out.println("[!] Sprint false");
                clientSprintMetadata = SplitStateBoolean.FALSE;
                if (!possibleOverride) { // The client updated their sprint which was 100% not triggered by the server
                    uncertainSprintAttribute = false;
                }
                break;
            case START_SNEAKING:
                sneaking = true;
                break;
            case STOP_SNEAKING:
                sneaking = false;
                break;
        }
    }

    public boolean handleEntityMetadata(WrapperPlayServerEntityMetadata wrapper) {
        if (data.getUser().getEntityId() != wrapper.getEntityId()) {
            return false;
        }

        // Cancel temporarily to debug
        if (true) {
            return true;
        }


        int bitmask = 0;

        List<EntityData> dataList = wrapper.getEntityMetadata();

        boolean found = false;
        boolean state = false;
        for (EntityData d : dataList) {
            if (d.getIndex() == 0) {
                found = true;
                bitmask = (byte) d.getValue();
                state = ((bitmask & 0x08) != 0);
                break;
            }
        }

        if (!found) {
            return false;
        }

        // Calculate the useful bitmask
        bitmask &= ~0x02; // Remove crouching
        bitmask &= ~0x08; // Remove sprinting
        bitmask &= ~0x10; // Remove using

        if (bitmask == lastUsefulMetadata) {
            return true;
        }

        if (lastSprintOverridePreTransaction == data.getTransactionTracker().getLastTransactionSent()) {
            data.getTransactionTracker().sendTransaction();
        }

        lastSprintOverridePreTransaction = data.getTransactionTracker().getLastTransactionSent();

        boolean newState = state;

        data.getTransactionTracker().pre(() -> {
            possibleOverride = true;
            serverSprintState = newState;

            if (!newState) {
                uncertainSprintAttribute = true;
            }
        });

        data.getTransactionTracker().post(() -> {
            resetOverride = true;
        });

        return false;
    }

    public void handleEndClientTick() {
        this.lastClientSprintMetadata = clientSprintMetadata;

        if (resetOverride) {
            resetOverride = false;
            possibleOverride = false;
        }
    }
}
