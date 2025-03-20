package me.beanes.acid.plugin.player;

import com.github.retrooper.packetevents.protocol.player.User;

import java.util.*;

public class PlayerManager {

    // A thread local variable so we don't need a ConcurrentHashMap (which does useless locking!) for the player data
    private final ThreadLocal<Map<User, PlayerData>> cacheThreadLocal = ThreadLocal.withInitial(HashMap::new);
    // This could even use a FastThreadLocal https://netty.io/4.0/api/io/netty/util/concurrent/FastThreadLocal.html

    public void add(PlayerData data) {
        Map<User, PlayerData> cache = cacheThreadLocal.get();
        cache.put(data.getUser(), data);
    }

    public void remove(User user) {
        Map<User, PlayerData> cache = cacheThreadLocal.get();
        cache.remove(user);
    }

    public PlayerData get(User user) {
        Map<User, PlayerData> cache = cacheThreadLocal.get();

        return cache.get(user);
    }

    public Collection<PlayerData> getPlayers() {
        return cacheThreadLocal.get().values();
    }
}
