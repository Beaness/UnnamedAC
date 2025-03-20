package me.beanes.acid.plugin.check;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import me.beanes.acid.plugin.check.impl.cloud.backtrack.BacktrackA;
import me.beanes.acid.plugin.check.impl.cloud.hitselect.HitSelectA;
import me.beanes.acid.plugin.check.impl.local.attack.AttackA;
import me.beanes.acid.plugin.check.impl.local.attack.AttackB;
import me.beanes.acid.plugin.check.impl.local.blink.Blink;
import me.beanes.acid.plugin.check.impl.local.simulation.Simulation;
import me.beanes.acid.plugin.check.impl.local.timer.Timer;
import me.beanes.acid.plugin.check.model.*;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.simulation.data.SimulationResult;

public class CheckManager {
    private final Object2ObjectMap<Class<? extends AbstractCheck>, AbstractCheck> clazzToCheckMap = new Object2ObjectOpenHashMap<>();
    @Getter
    private PreReceivePacketCheck[] preReceivePacketChecks;
    @Getter
    private ReceivePacketCheck[] receivePacketChecks;
    @Getter
    private SendPacketCheck[] sendPacketChecks;
    @Getter
    private SimulationCheck[] simulationChecks;

    public CheckManager(PlayerData data) {
        AbstractCheck[] checks = new AbstractCheck[] {
                new AttackA(data),
                new AttackB(data),
                new BacktrackA(data),
                // new HitSelectA(data),
                new Timer(data),
                new Blink(data),
                new Simulation(data)
        };

        this.registerChecks(checks);
    }

    private void register(AbstractCheck check) {
        clazzToCheckMap.put(check.getClass(), check);
    }

    private void registerChecks(AbstractCheck[] checks) {
        int preReceiveChecks = 0;
        int receiveChecks = 0;
        int sendChecks = 0;
        int simChecks = 0;

        // First count the checks to make sure we have the right array sizes
        for (AbstractCheck check : checks) {
            if (check instanceof PreReceivePacketCheck) {
                preReceiveChecks++;
            }

            if (check instanceof ReceivePacketCheck) {
                receiveChecks++;
            }

            if (check instanceof SendPacketCheck) {
                sendChecks++;
            }

            if (check instanceof SimulationCheck) {
                simChecks++;
            }

            this.register(check);
        }

        preReceivePacketChecks = new PreReceivePacketCheck[preReceiveChecks];
        receivePacketChecks = new ReceivePacketCheck[receiveChecks];
        sendPacketChecks = new SendPacketCheck[sendChecks];
        simulationChecks = new SimulationCheck[simChecks];


        for (AbstractCheck check : checks) {
            if (check instanceof PreReceivePacketCheck) {
                preReceivePacketChecks[--preReceiveChecks] = (PreReceivePacketCheck) check;
            }

            if (check instanceof ReceivePacketCheck) {
                receivePacketChecks[--receiveChecks] = (ReceivePacketCheck) check;
            }

            if (check instanceof SendPacketCheck) {
                sendPacketChecks[--sendChecks] = (SendPacketCheck) check;
            }

            if (check instanceof SimulationCheck) {
                simulationChecks[--simChecks] = (SimulationCheck) check;
            }
        }
    }

    public <T extends AbstractCheck> T getCheck(Class<T> clazz) {
        return (T) clazzToCheckMap.get(clazz);
    }

    // Runs before all trackers
    public void preReceivePacket(PacketReceiveEvent event) {
        for (PreReceivePacketCheck check : preReceivePacketChecks) {
            check.onPacketPreReceive(event);
        }
    }

    public void receivePacket(PacketReceiveEvent event) {
        for (ReceivePacketCheck check : receivePacketChecks) {
            check.onPacketReceive(event);
        }
    }

    public void sendPacket(PacketSendEvent event) {
        for (SendPacketCheck check : sendPacketChecks) {
            check.onPacketSend(event);
        }
    }

    public void onSimulate(SimulationResult result) {
        for (SimulationCheck check : simulationChecks) {
            check.onSimulation(result);
        }
    }
}
