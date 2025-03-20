package me.beanes.acid.plugin.mitigate;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.cloud.packet.impl.player.PlayerJoinPacket;
import me.beanes.acid.plugin.cloud.packet.impl.player.PlayerMitigateDamagePacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class MitigationManager implements Listener {
    private final Object2DoubleMap<UUID> attackDamageModifier = new Object2DoubleOpenHashMap<>();
    private final Object2DoubleMap<UUID> receiveDamageModifier = new Object2DoubleOpenHashMap<>();

    public MitigationManager() {
        Bukkit.getPluginManager().registerEvents(this, Acid.get());
    }

    public void handleDamageMitigatePacket(PlayerMitigateDamagePacket packet) {
        UUID player = packet.getPlayer();
        double attackModifier = packet.getAttackingDamageModifier();
        double receiveModifier = packet.getIncomingDamageModifier();

        // Sync to main thread
        Bukkit.getScheduler().runTask(Acid.get(), () -> {
            if (attackModifier != 1.0D || receiveModifier != 1.0D) {
                Bukkit.broadcastMessage("Player " + packet.getPlayer() + " has been mitigated with: " + attackModifier);
            }

            applyAttackDamageModifier(player, attackModifier);
            applyReceiveDamageModifier(player, receiveModifier);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleDamageEvent(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            event.setDamage(0.0D);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleFoodChangeEvent(FoodLevelChangeEvent event) {
        event.setFoodLevel(20);
    }

    private void applyAttackDamageModifier(UUID uuid, double damage) {
        if (damage == 1.0D) {
            attackDamageModifier.removeDouble(uuid);
            return;
        }

        attackDamageModifier.put(uuid, damage);
    }

    private void applyReceiveDamageModifier(UUID uuid, double damage) {
        if (damage == 1.0D) {
            receiveDamageModifier.removeDouble(uuid);
            return;
        }

        receiveDamageModifier.put(uuid, damage);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();

            Player attacker = null;

            if (event.getDamager() instanceof Arrow) {
                Arrow arrow = (Arrow) event.getDamager();

                if (arrow.getShooter() instanceof Player) {
                    attacker = (Player) arrow.getShooter();
                }
            } else if (event.getDamager() instanceof Player) {
                attacker = (Player) event.getDamager();
            }

            if (attacker != null) {
                // Bukkit.broadcastMessage("attacker= " + attacker.getUniqueId());

                double attackModifier = attackDamageModifier.getOrDefault(attacker.getUniqueId(), 1.0D);
                double receiveModifier = receiveDamageModifier.getOrDefault(victim.getUniqueId(), 1.0D);

                // Bukkit.broadcastMessage("modifier=" + attackModifier + " dmg=" + event.getDamage());

                event.setDamage(event.getDamage() * attackModifier * receiveModifier);

                // Bukkit.broadcastMessage("finalDmg=" + event.getDamage());
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        attackDamageModifier.removeDouble(event.getPlayer().getUniqueId());
        receiveDamageModifier.removeDouble(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // TODO: remove this!!
        Acid.get().getAlertsManager().enableAlerts(event.getPlayer().getUniqueId());
        Acid.get().getAlertsManager().enableSimulationAlerts(event.getPlayer().getUniqueId());

        Acid.get().getCloudManager().sendPacket(new PlayerJoinPacket(event.getPlayer().getUniqueId()));
    }
}
