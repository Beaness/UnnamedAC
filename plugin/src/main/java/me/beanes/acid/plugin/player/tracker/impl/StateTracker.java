package me.beanes.acid.plugin.player.tracker.impl;

import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClientStatus;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSelectTrade;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import lombok.Getter;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.player.tracker.Tracker;
import me.beanes.acid.plugin.util.splitstate.ConfirmableState;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

@Getter
public class StateTracker extends Tracker {
    public StateTracker(PlayerData data) {
        super(data);
    }

    private final ConfirmableState<Float> health = new ConfirmableState<>(20F);
    private final ConfirmableState<Integer> food = new ConfirmableState<>(20);
    private final ConfirmableState<GameMode> gameMode = new ConfirmableState<>(GameMode.SURVIVAL);

    private float lastHealth, lastFood;

    // Returns if the packet should be cancelled
    public boolean handleUpdateHealth(WrapperPlayServerUpdateHealth wrapper) {
        float newHealth = wrapper.getHealth();
        int newFood = wrapper.getFood();

        // This code was put in place to prevent dumb spigot packet spamming health & food with saturation effect
        if (lastHealth == newHealth && lastFood == newFood) {
            return true;
        }

        lastHealth = newHealth;
        lastFood = newFood;

        health.checkTransaction(data);
        food.checkTransaction(data);

        data.getTransactionTracker().pre(() -> {
            System.out.println("-> Transaction health: " + newHealth);

            health.setValue(newHealth);
            food.setValue(newFood);
        });

        data.getTransactionTracker().post(() -> {
            health.confirm();
            food.confirm();
        });

        return false;
    }

    public void handleGameStateChange(WrapperPlayServerChangeGameState wrapper) {
        if (wrapper.getReason() != WrapperPlayServerChangeGameState.Reason.CHANGE_GAME_MODE) {
            return;
        }

        gameMode.checkTransaction(data);

        GameMode mode = GameMode.getById((int) wrapper.getValue());

        data.getTransactionTracker().pre(() -> {
            gameMode.setValue(mode);
        });

        data.getTransactionTracker().post(gameMode::confirm);
    }

    public void handleRespawn(WrapperPlayServerRespawn wrapper) {
        health.checkTransaction(data);
        food.checkTransaction(data);
        gameMode.checkTransaction(data);

        data.getTransactionTracker().pre(() -> {
            health.setValue(20F);
            food.setValue(20);
            gameMode.setValue(wrapper.getGameMode());
        });

        data.getTransactionTracker().post(() -> {
            health.confirm();
            food.confirm();
            gameMode.confirm();
        });
    }

    public void handleJoinGame(WrapperPlayServerJoinGame wrapper) {
        gameMode.setValue(wrapper.getGameMode());
        gameMode.confirm();
    }

    public SplitStateBoolean isCreative() {
        boolean latest = gameMode.getValue() == GameMode.CREATIVE;
        boolean old = gameMode.getOldValue() != null ? gameMode.getOldValue() == GameMode.CREATIVE : latest;

        return SplitStateBoolean.result(latest, old);
    }
}
