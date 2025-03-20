package me.beanes.acid.plugin.util;

import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.player.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.spigotmc.event.entity.EntityMountEvent;

public class UnsupportedUtil implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void mountListener(EntityMountEvent event) {
        event.setCancelled(true);
        Acid.get().getLogger().warning("Entity mounting is currently not supported!");

        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            Acid.get().adventure().sender(player).sendMessage(
                    Component.join(
                            JoinConfiguration.noSeparators(),
                            Component.text("⚒", NamedTextColor.DARK_RED),
                            Component.text(" » ", NamedTextColor.GRAY),
                            Component.text("Sorry but riding entities is disabled currently", NamedTextColor.RED)
                    )
            );
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void rightClickListener(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            if (event.getItem() != null && event.getItem().getType() == Material.BOAT) {
                event.setCancelled(true);

                Acid.get().adventure().sender(event.getPlayer()).sendMessage(
                        Component.join(
                                JoinConfiguration.noSeparators(),
                                Component.text("⚒", NamedTextColor.DARK_RED),
                                Component.text(" » ", NamedTextColor.GRAY),
                                Component.text("Sorry but boats are currently disabled", NamedTextColor.RED)
                        )
                );
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof Boat) {
            event.setCancelled(true);
            Acid.get().getLogger().warning("Boats are not supported currently!");
        }
    }
}
