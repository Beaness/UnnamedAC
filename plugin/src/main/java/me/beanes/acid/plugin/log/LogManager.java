package me.beanes.acid.plugin.log;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.cloud.Log;
import me.beanes.acid.plugin.cloud.packet.impl.log.LogPacket;
import me.beanes.acid.plugin.bson.BsonBinaryUtil;
import org.bson.Document;
import org.bukkit.Bukkit;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogManager {
    private static final ExecutorService LOG_MANAGER = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("log-manager").build());
    private static final int FLUSH_MAX_AMOUNT = 500;
    private static final int FLUSH_UPDATE_TICKS = 20 * 10;
    private static final int MAX_QUEUE_SIZE = 10_000;
    private final Queue<Log> queue = new ArrayDeque<>();

    public LogManager() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(Acid.get(), () -> {
            LOG_MANAGER.execute(this::checkForFlush);
        }, 0L, FLUSH_UPDATE_TICKS);
    }

    public void createLogAndEnqueue(UUID player, String checkName, Document flagData) {
        LOG_MANAGER.execute(() -> {
            byte[] bytes = BsonBinaryUtil.toBytes(flagData);
            Log log = new Log(player, Instant.now(), checkName, bytes);
            Acid.get().getLogManager().enqueue(log);
        });
    }

    private void enqueue(Log log) {
        queue.add(log);

        if (queue.size() > MAX_QUEUE_SIZE) { // Protect against a long backfill if the cloud is down
            queue.remove();
        }
    }

    public void checkForFlush() {
        // Don't write logs if the cloud is not connected
        if (!Acid.get().getCloudManager().isConnected()) {
            return;
        }

        if (queue.isEmpty()) {
            return;
        }

        flushQueue();
    }

    private void flushQueue() {
        int toSent = Math.min(FLUSH_MAX_AMOUNT, queue.size());

        Log[] sending = new Log[toSent];

        for (int i = 0; i < toSent; i++) {
            Log log = queue.remove();
            sending[i] = log;
        }

        Acid.get().getCloudManager().sendPacket(new LogPacket(sending));
    }
}
