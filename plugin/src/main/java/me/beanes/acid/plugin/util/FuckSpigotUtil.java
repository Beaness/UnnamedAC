package me.beanes.acid.plugin.util;

import me.beanes.acid.plugin.Acid;

public class FuckSpigotUtil {
    public static void setThresholdsToInfinite() {
        try{
            Class.forName("org.spigotmc.SpigotConfig")
                    .getDeclaredField("movedTooQuicklyThreshold")
                    .setDouble(null, Double.MAX_VALUE);

            Class.forName("org.spigotmc.SpigotConfig")
                    .getDeclaredField("movedWronglyThreshold")
                    .setDouble(null, Double.MAX_VALUE);
        } catch (Exception exception) {
            Acid.get().getLogger().info("Failed to disable spigot tresholds");
        }

    }
}
