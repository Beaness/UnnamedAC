package me.beanes.acid.plugin.alert;

import me.beanes.acid.plugin.Acid;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AlertsManager {
    private final Set<UUID> enabledAlerts = new HashSet<>();
    private final Set<UUID> enabledSimulationAlerts = new HashSet<>();

    public AlertsManager() {
        enabledAlerts.add(Acid.CONSOLE_UUID); // Enable alerts for console by default
        enabledAlerts.add(UUID.fromString("759c7730-4702-4bb1-85c0-d6be18069c59"));
        enabledSimulationAlerts.add(UUID.fromString("759c7730-4702-4bb1-85c0-d6be18069c59"));
    }

    public void enableAlerts(UUID uuid) {
        enabledAlerts.add(uuid);
    }

    public void enableSimulationAlerts(UUID uuid) {
        enabledSimulationAlerts.add(uuid);
    }

    public void disableAlerts(UUID uuid) {
        enabledAlerts.remove(uuid);
    }

    public void disableSimulationAlerts(UUID uuid) {
        enabledSimulationAlerts.add(uuid);
    }

    public boolean hasAlertsEnabled(UUID uuid) {
        return enabledAlerts.contains(uuid);
    }

    public boolean hasSimulationAlertsEnabled(UUID uuid) {
        return enabledSimulationAlerts.contains(uuid);
    }

    private static final HoverEvent<Component> HOVER_EVENT = HoverEvent.showText(
            Component.text("Click to teleport to this player", NamedTextColor.YELLOW)
    );

    private static final Component PREFIX = Component.text("✄", NamedTextColor.GOLD)
            .append(Component.text(" » ", NamedTextColor.GRAY));

    public void sendCertainAlert(String name, String simpleCheckName) {
        Component message = Component.join(
                JoinConfiguration.noSeparators(),
                PREFIX,
                Component.text(name, NamedTextColor.GOLD),
                Component.text(" was detected using ", NamedTextColor.YELLOW),
                Component.text(simpleCheckName, NamedTextColor.GOLD),
                Component.text(".", NamedTextColor.YELLOW)
        ).hoverEvent(HOVER_EVENT).clickEvent(ClickEvent.suggestCommand("/tp " + name));

        broadcast(message);
    }

    public void sendNeutralAlert(String name, String simpleCheckName) {
        Component message = Component.join(
                JoinConfiguration.noSeparators(),
                PREFIX,
                Component.text(name, NamedTextColor.GOLD),
                Component.text(" failed ", NamedTextColor.YELLOW),
                Component.text(simpleCheckName, NamedTextColor.GOLD),
                Component.text(".", NamedTextColor.YELLOW)
        ).hoverEvent(HOVER_EVENT).clickEvent(ClickEvent.suggestCommand("/tp " + name));

        broadcast(message);
    }

    public void sendUncertainAlert(String name, String simpleCheckName) {
        Component message = Component.join(
                JoinConfiguration.noSeparators(),
                PREFIX,
                Component.text(name, NamedTextColor.GOLD),
                Component.text(" might be using ", NamedTextColor.YELLOW),
                Component.text(simpleCheckName, NamedTextColor.GOLD),
                Component.text(".", NamedTextColor.YELLOW)
        ).hoverEvent(HOVER_EVENT).clickEvent(ClickEvent.suggestCommand("/tp " + name));

        broadcast(message);
    }

    public void broadcast(Component message) {
        Bukkit.getScheduler().runTask(Acid.get(), () -> {
            Acid.get().adventure().console().sendMessage(message);

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!hasAlertsEnabled(player.getUniqueId())) {
                    return;
                }

                if (player.hasPermission("acid.alerts")) {
                    Acid.get().getAdventure().sender(player).sendMessage(message);
                }
            }
        });
    }
}
