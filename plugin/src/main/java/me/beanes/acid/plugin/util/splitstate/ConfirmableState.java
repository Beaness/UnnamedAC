package me.beanes.acid.plugin.util.splitstate;

import lombok.Getter;
import me.beanes.acid.plugin.player.PlayerData;

public class ConfirmableState<T> {
    @Getter
    private T value;
    @Getter
    private T oldValue;
    private int lastPreTransaction;

    public ConfirmableState(T startValue) {
        this.value = startValue;
    }

    public void setValue(T value) {
        if (value == null){
            throw new IllegalStateException("Confirmable States do not support null values");
        }

        this.oldValue = this.value;
        this.value = value;
    }

    public void setValueImmediate(T value) {
        if (value == null){
            throw new IllegalStateException("Confirmable States do not support null values");
        }

        this.value = value;
    }

    public void setOldValueImmediate(T value) {
        this.oldValue = value;
    }

    public void setValueCertainly(T value) {
        if (value == null){
            throw new IllegalStateException("Confirmable States do not support null values");
        }

        this.value = value;
        this.confirm();
    }

    public void confirm() {
        this.oldValue = null;
    }

    public void checkTransaction(PlayerData data) {
        if (this.lastPreTransaction == data.getTransactionTracker().getLastTransactionSent()) {
            data.getTransactionTracker().sendTransaction();
        }
        this.lastPreTransaction = data.getTransactionTracker().getLastTransactionSent();
    }
}
