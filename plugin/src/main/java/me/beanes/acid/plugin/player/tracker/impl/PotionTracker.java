package me.beanes.acid.plugin.player.tracker.impl;

import com.github.retrooper.packetevents.protocol.potion.PotionType;
import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRemoveEntityEffect;
import lombok.Getter;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.player.tracker.Tracker;
import me.beanes.acid.plugin.util.splitstate.ConfirmableState;

@Getter
public class PotionTracker extends Tracker {
    public PotionTracker(PlayerData data) {
        super(data);
    }

    // It is possible that a potion update transaction is split so we keep a split state
    // We only track jump boost amplifier since speed and slowness are both tracked by modifiers and only have a visual effect with the potion packet
    private final ConfirmableState<Integer> jumpBoostAmplifier = new ConfirmableState<>(0);
    private final ConfirmableState<Integer> hasteAmplifier = new ConfirmableState<>(0);
    private final ConfirmableState<Integer> miningFatigueAmplifier = new ConfirmableState<>(0);

    public void handleEffect(final WrapperPlayServerEntityEffect wrapper) {
        if (wrapper.getEntityId() == data.getUser().getEntityId()) {
            ConfirmableState<Integer> potionAmplifier = getConfirmableState(wrapper.getPotionType());
            if (potionAmplifier != null) {
                potionAmplifier.checkTransaction(data);

                int amplifier = wrapper.getEffectAmplifier();

                data.getTransactionTracker().pre(() -> {
                    potionAmplifier.setValue(amplifier + 1);
                });
                data.getTransactionTracker().post(potionAmplifier::confirm);
            }
        }
    }

    public void handleRemoveEffect(final WrapperPlayServerRemoveEntityEffect wrapper) {
        if (wrapper.getEntityId() == data.getUser().getEntityId()) {
            ConfirmableState<Integer> potionAmplifier = getConfirmableState(wrapper.getPotionType());
            if (potionAmplifier != null) {
                potionAmplifier.checkTransaction(data);

                data.getTransactionTracker().pre(() -> {
                    potionAmplifier.setValue(0);
                });
                data.getTransactionTracker().post(potionAmplifier::confirm);
            }
        }
    }

    public void handleRespawn() {
        jumpBoostAmplifier.checkTransaction(data);

        data.getTransactionTracker().pre(() -> {
            jumpBoostAmplifier.setValue(0);
            hasteAmplifier.setValue(0);
            miningFatigueAmplifier.setValue(0);
        });

        data.getTransactionTracker().post(jumpBoostAmplifier::confirm);
    }

    public ConfirmableState<Integer> getConfirmableState(PotionType potionType) {
        if (potionType == PotionTypes.JUMP_BOOST) {
            return jumpBoostAmplifier;
        }

        if (potionType == PotionTypes.HASTE) {
            return hasteAmplifier;
        }

        if (potionType == PotionTypes.MINING_FATIGUE) {
            return miningFatigueAmplifier;
        }

        return null;
    }
}
