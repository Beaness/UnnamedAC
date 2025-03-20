package me.beanes.acid.plugin.check.model;

import lombok.Getter;
import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.player.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractCheck {
    @Getter
    protected final PlayerData data;
    protected final String name;

    protected AbstractCheck(PlayerData data, String name) {
        this.data = data;
        this.name = name;
    }

    public void debug(String original) {
        if (!Acid.DEBUG) {
            return;
        }

        String msg = original.replaceAll("&", "§"); // Replace all & color codes with §
        msg = msg.replaceAll("\\b\\d+(\\.\\d+)?\\b", "§e$0§6"); // Prefix all numbers with §e



        // All of this is not threadsafe ig but I'd rather have debug messages instantly
        Component debugMsg = Component.join(
                JoinConfiguration.noSeparators(),
                Component.text("⚒", NamedTextColor.GOLD),
                Component.text(" » ", NamedTextColor.GRAY),
                Component.text().append(LegacyComponentSerializer.legacySection().deserialize(msg).color(NamedTextColor.GOLD)
        ).hoverEvent(HoverEvent.showText(Component.text("Click to copy this debug caused by the check: ").append(Component.text(name, NamedTextColor.YELLOW)))).clickEvent(ClickEvent.suggestCommand(original)));

        Acid.get().adventure().console().sendMessage(debugMsg);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("acid.debug")) {
                Acid.get().getAdventure().sender(player).sendMessage(debugMsg);
            }
        }
    }
}
