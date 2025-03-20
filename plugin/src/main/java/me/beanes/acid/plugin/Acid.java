package me.beanes.acid.plugin;

import com.github.retrooper.packetevents.PacketEvents;
import lombok.Getter;
import me.beanes.acid.plugin.alert.AlertsManager;
import me.beanes.acid.plugin.cloud.CloudManager;
import me.beanes.acid.plugin.command.*;
import me.beanes.acid.plugin.log.LogManager;
import me.beanes.acid.plugin.mitigate.MitigationManager;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.player.PlayerManager;
import me.beanes.acid.plugin.player.listener.PacketEventsListener;
import me.beanes.acid.plugin.block.BlockManager;
import me.beanes.acid.plugin.util.FuckSpigotUtil;
import me.beanes.acid.plugin.util.UnsupportedUtil;
import me.beanes.acid.plugin.util.pledge.TickEndUtil;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

@Getter
public final class Acid extends JavaPlugin {
    public static final UUID CONSOLE_UUID = UUID.fromString("00000000-0000-0000-8000-000000000000");
    public static final boolean DEBUG = true;

    private static Acid INSTANCE;

    private BukkitAudiences adventure;
    private PlayerManager playerManager;
    private BlockManager blockManager;
    private MitigationManager mitigationManager;
    private LogManager logManager;
    private CloudManager cloudManager;
    private AlertsManager alertsManager;

    public Acid() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        this.adventure = BukkitAudiences.create(this);

        this.playerManager = new PlayerManager();
        this.blockManager = new BlockManager();
        this.mitigationManager = new MitigationManager();
        this.logManager = new LogManager();
        this.cloudManager = new CloudManager();
        this.alertsManager = new AlertsManager();

        FuckSpigotUtil.setThresholdsToInfinite();

        PacketEvents.getAPI().getSettings().reEncodeByDefault(false).debug(true).fullStackTrace(true);

        // Sends a transaction for every player at the tick start of the server
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (PlayerData data : playerManager.getPlayers()) {
                data.getTransactionTracker().sendTransactionWithEventLoop();
            }
        }, 0L, 1L);

        // Hook into first tick incase the server has late bind enabled
        Bukkit.getScheduler().runTask(Acid.get(), () -> {
            PacketEvents.getAPI().getSettings().reEncodeByDefault(false).debug(true).fullStackTrace(true);

            // Sends a transaction for every player at the tick end / just before the player's netty channel is flushed
            TickEndUtil.injectRunnable(() -> {
                for (PlayerData data : playerManager.getPlayers()) {
                    data.getTransactionTracker().sendTransactionWithEventLoop();
                }
            });
        });

        Bukkit.getPluginCommand("acid").setExecutor(new MainCommand());
        Bukkit.getPluginManager().registerEvents(new UnsupportedUtil(), this);
    }

    @Override
    public void onDisable() {
        this.logManager.checkForFlush(); // Flush the last packets
    }

    @Override
    public void onLoad() {
        PacketEvents.getAPI().getEventManager().registerListener(new PacketEventsListener());
    }

    public static Acid get() {
        return INSTANCE;
    }

    public BukkitAudiences adventure() {
        return this.adventure;
    }
}
