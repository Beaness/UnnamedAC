package me.beanes.acid.plugin.player.log;

import me.beanes.acid.plugin.player.PlayerData;

import java.util.ArrayDeque;
import java.util.Queue;

public class LogManager {
    private PlayerData data;
    private Queue<Long> queuedWrite;

    public LogManager(PlayerData data) {
        this.data = data;
        this.queuedWrite = new ArrayDeque<>();
    }

    public void alert() {


    }
}
