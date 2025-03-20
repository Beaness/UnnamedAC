package me.beanes.acid.plugin.player.tracker.impl;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import lombok.Getter;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.player.tracker.Tracker;
import me.beanes.acid.plugin.util.MCMath;
import me.beanes.acid.plugin.util.splitstate.ConfirmableState;

import java.util.UUID;

public class AttributeTracker extends Tracker {
    public AttributeTracker(PlayerData data) {
        super(data);
    }

    // TODO: check if attribute change gets delayed by a client tick?
    private static final UUID SPRINT_ATTRIBUTE = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");
    @Getter
    private final ConfirmableState<Double> movementSpeed = new ConfirmableState<>((double) 0.1F);
    @Getter
    private final ConfirmableState<Double> movementSpeedWithSprint = new ConfirmableState<>(0.13000000312924387D);
    private double lastSentSpeed = 0.1F;

    /*
        So we basically calculate the movement speed if the player was sprinting and if the player was not sprinting
        This is because we simulate both sprinting and no sprinting because of sprint desyncs

        If the server sends attributes it overrides all the client attributes, the problem is the sprint attribute is applied clientside also

        This method returns true if the attribute packet should be cancelled (as its an useless packet anyways)
     */

    public boolean handleUpdateAttribute(final WrapperPlayServerUpdateAttributes wrapper) {
        if (data.getUser().getEntityId() == wrapper.getEntityId()) {
            for (WrapperPlayServerUpdateAttributes.Property snapshot : wrapper.getProperties()) {
                if (snapshot.getAttribute().getName().getKey().equals("movement_speed")) {
                    double newSpeed = snapshot.getValue();

                    // First do all modifiers with operation 0 and 1
                    for (WrapperPlayServerUpdateAttributes.PropertyModifier modifier : snapshot.getModifiers()) {
                        // If some fucked up person sends sprint as a different modifier operator they can kill themselves TODO: fix this?
                        if (modifier.getUUID().equals(SPRINT_ATTRIBUTE)) continue;

                        if (modifier.getOperation() == WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.ADDITION) {
                            newSpeed += modifier.getAmount();
                        }

                        if (modifier.getOperation() == WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.MULTIPLY_BASE) {
                            newSpeed += snapshot.getValue() * modifier.getAmount();
                        }
                    }

                    double newSpeedWithSprint = 0;
                    boolean serverSentSprint = false;

                    // Lastly do all modifiers with operation 2
                    for (WrapperPlayServerUpdateAttributes.PropertyModifier modifier : snapshot.getModifiers()) {
                        if (modifier.getOperation() == WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.MULTIPLY_TOTAL) {
                            // Server send the sprint attribute, let's pray this is just the vanilla value...
                            if (modifier.getUUID().equals(SPRINT_ATTRIBUTE)) {
                                newSpeedWithSprint = newSpeed * (1.0D + 0.30000001192092896);
                                serverSentSprint = true;
                                continue;
                            }

                            newSpeed *= 1 + modifier.getAmount();
                            if (serverSentSprint) {
                                newSpeedWithSprint *= 1.0D + modifier.getAmount();
                            }
                        }
                    }

                    // So the server did not send the sprint attribute, but it's always possible that the client applies it clientside so let's precalculate it
                    if (!serverSentSprint) {
                        newSpeedWithSprint = newSpeed * (1.0D + 0.30000001192092896);
                    }

                    newSpeed = MCMath.clamp_double(newSpeed, 0.0D, 1024.0D);
                    newSpeedWithSprint = MCMath.clamp_double(newSpeedWithSprint, 0.0D, 1024.0D);

                    // Don't update the attributes for just the same speed (this should help A LOT with sprint attribute desyncing)
                    if (newSpeed == this.lastSentSpeed) {
                        return true;
                    }

                    this.lastSentSpeed = newSpeed;


                    movementSpeed.checkTransaction(data);
                    movementSpeedWithSprint.checkTransaction(data);

                    double finalNewSpeed = newSpeed;
                    double finalNewSpeedWithSprint = newSpeedWithSprint;
                    data.getTransactionTracker().pre(() -> {
                        movementSpeed.setValue(finalNewSpeed);
                        movementSpeedWithSprint.setValue(finalNewSpeedWithSprint);
                    });

                    data.getTransactionTracker().post(() -> {
                        movementSpeed.confirm();
                        movementSpeedWithSprint.confirm();
                    });
                }
            }
        }

        return false;
    }
}
