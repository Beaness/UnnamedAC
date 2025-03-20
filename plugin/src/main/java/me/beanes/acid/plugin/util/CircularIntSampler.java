package me.beanes.acid.plugin.util;

import lombok.Getter;

// A sort of circular buffer (without FIFO) that fills it samples and filling when its full will override the oldest valuel
public class CircularIntSampler {
    private final int[] data;
    private int index;
    @Getter
    private boolean full;

    public CircularIntSampler(int size) {
        data = new int[size];
    }

    public void fill(int value) {
        data[index++] = value;

        if (index == data.length) {
            index = 0;
            full = true;
        }
    }

    public int[] cloneAndEmpty() {
        if (!full) {
            throw new IllegalStateException("The sampler has not finished yet");
        }

        int[] cloned = data.clone();
        this.empty();
        return cloned;
    }

    public void empty() {
        index = 0;
        full = false;
    }
}
