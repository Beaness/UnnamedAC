package me.beanes.acid.plugin.player.tracker.impl.world.resync;

import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.player.tracker.impl.world.WorldTracker;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.Queue;

public class WorldResyncHandler {
    private final PlayerData data;

    public WorldResyncHandler(PlayerData data) {
        this.data = data;
    }

    private final Long2ObjectMap<Queue<QueuedResync>> queuedResyncs = new Long2ObjectOpenHashMap<>();

    public void scheduleResync(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    processBlockResync(x, y, z);
                }
            }
        }

        registerPostFlushTask();
    }

    public void scheduleResync(int x, int y, int z) {
        processBlockResync(x, y, z);
        registerPostFlushTask();
    }

    private void processBlockResync(int x, int y, int z) {
        long xz = WorldTracker.encodeToLong(x >> 4, z >> 4);
        int xInChunk = x & 15;
        int yInChunk = y & 255;
        int zInChunk = z & 15;
        short compactedBlockPos = (short) (yInChunk << 8 | zInChunk << 4 | xInChunk);

        Object channel = data.getUser().getChannel();
        World originalWorld = Bukkit.getPlayer(data.getUser().getUUID()).getWorld();

        Bukkit.getScheduler().runTask(Acid.get(), () -> {
            Player player = Bukkit.getPlayer(data.getUser().getUUID());

            if (player == null) {
                return;
            }

            if (player.getWorld() != originalWorld) {
                return;
            }

            Block block = player.getWorld().getBlockAt(x, y, z);
            int blockId = (block.getType().getId() << 4) | block.getData();

            ChannelHelper.runInEventLoop(channel, () -> {
                Queue<QueuedResync> queue = queuedResyncs.get(xz);

                if (queue == null) {
                    queue = new ArrayDeque<>();
                    queuedResyncs.put(xz, queue);
                }

                queue.add(new QueuedResync(compactedBlockPos, blockId));
            });
        });
    }

    private void registerPostFlushTask() {
        Object channel = data.getUser().getChannel();
        // After the resync queue is built this task should run
        Bukkit.getScheduler().runTask(Acid.get(), () -> {
            ChannelHelper.runInEventLoop(channel, this::flushQueue);
        });
    }

    private void flushQueue() {
        if (queuedResyncs.isEmpty()) {
            return;
        }

        for (Long2ObjectMap.Entry<Queue<QueuedResync>> entry : queuedResyncs.long2ObjectEntrySet()) {
            long xz = entry.getLongKey();
            Queue<QueuedResync> blockQueue = entry.getValue();

            int chunkX = WorldTracker.getX(xz);
            int chunkZ = WorldTracker.getZ(xz);

            if (blockQueue.size() == 1) {
                QueuedResync queuedResync = blockQueue.poll();
                short compactedBlockPos = queuedResync.getCompactedBlockPos();
                int blockId = queuedResync.getBlockId();

                int yInChunk = (compactedBlockPos >> 8) & 255;
                int zInChunk = (compactedBlockPos >> 4) & 15;
                int xInChunk = compactedBlockPos & 15;

                int realX = (chunkX << 4) + xInChunk;
                int realZ = (chunkZ << 4) + zInChunk;

                Vector3i vector3i = new Vector3i(realX, yInChunk, realZ);

                data.getUser().writePacket(new WrapperPlayServerBlockChange(vector3i, blockId));
            } else {
                WrapperPlayServerMultiBlockChange.EncodedBlock[] encodedBlocks = new WrapperPlayServerMultiBlockChange.EncodedBlock[blockQueue.size()];

                int i = 0;
                for (QueuedResync queuedResync : blockQueue) {
                    short compactedBlockPos = queuedResync.getCompactedBlockPos();
                    int yInChunk = (compactedBlockPos >> 8) & 255;
                    int zInChunk = (compactedBlockPos >> 4) & 15;
                    int xInChunk = compactedBlockPos & 15;

                    encodedBlocks[i++] = new WrapperPlayServerMultiBlockChange.EncodedBlock(queuedResync.getBlockId(), xInChunk, yInChunk, zInChunk);
                }

                data.getUser().writePacket(new WrapperPlayServerMultiBlockChange(new Vector3i(chunkX, 0, chunkZ), false, encodedBlocks));
            }
        }

        data.getUser().flushPackets();

        queuedResyncs.clear();
    }
}
