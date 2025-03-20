package me.beanes.acid.plugin.util;

import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.UUID;

public class ReleaseItemUtil {
    private static Method getHandleMethod;
    private static Method releaseItemField;

    public static void releaseOnMainThread(PlayerData data) {
        UUID uuid = data.getUser().getUUID();
        Bukkit.getScheduler().runTask(Acid.get(), () -> {
            Player player = Bukkit.getPlayer(uuid);

            if (player != null) {
                release(player);
            }
        });
    }

    private static void release(Player player) {
        try {
            if (getHandleMethod == null) {
                getHandleMethod = player.getClass().getDeclaredMethod("getHandle");
            }

            Object entityPlayer = getHandleMethod.invoke(player);
            if (releaseItemField == null) {
                releaseItemField = entityPlayer.getClass().getSuperclass().getDeclaredMethod("bU");
            }

            releaseItemField.invoke(entityPlayer);
            Bukkit.broadcastMessage("ITEM RELEASE YESS <-)<");
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.broadcastMessage("SHUTDOWN");
        }
    }
}
