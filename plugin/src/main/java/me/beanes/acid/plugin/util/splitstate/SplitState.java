package me.beanes.acid.plugin.util.splitstate;

import lombok.Getter;

@Getter
public class SplitState<T> {
    private final T value;
    private final T oldValue;

    public SplitState(T value, T oldValue) {
        this.value = value;
        this.oldValue = oldValue;
    }

    @Override
    public String toString() {
        return "SplitState{" +
                "value=" + value +
                ", oldValue=" + oldValue +
                '}';
    }
}