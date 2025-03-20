package me.beanes.acid.plugin.player;

import com.github.retrooper.packetevents.protocol.player.User;
import lombok.Getter;
import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.check.CheckManager;
import me.beanes.acid.plugin.cloud.packet.impl.player.PlayerRequestMitigationPacket;
import me.beanes.acid.plugin.player.tracker.impl.*;
import me.beanes.acid.plugin.player.tracker.impl.entity.EntityTracker;
import me.beanes.acid.plugin.player.tracker.impl.inventory.InventoryTracker;
import me.beanes.acid.plugin.player.tracker.impl.position.PositionTracker;
import me.beanes.acid.plugin.player.tracker.impl.position.SetbackTracker;
import me.beanes.acid.plugin.player.tracker.impl.velocity.VelocityTracker;
import me.beanes.acid.plugin.player.tracker.impl.world.WorldTracker;
import me.beanes.acid.plugin.simulation.SimulationEngine;
import me.beanes.acid.plugin.util.trig.TrigHandler;

@Getter
public class PlayerData {

    private final User user;
    private final CheckManager checkManager;
    private final TrigHandler trigHandler;
    private final TransactionTracker transactionTracker;
    private final PositionTracker positionTracker;
    private final RotationTracker rotationTracker;
    private final StateTracker stateTracker;
    private final UsingTracker usingTracker;
    private final ActionTracker actionTracker;
    private final AttributeTracker attributeTracker;
    private final PotionTracker potionTracker;
    private final InventoryTracker inventoryTracker;
    private final WorldTracker worldTracker;
    private final EntityTracker entityTracker;
    private final AbilitiesTracker abilitiesTracker;
    private final VelocityTracker velocityTracker;
    private final RespawnTracker respawnTracker;
    private final SetbackTracker setbackTracker;
    private final SimulationEngine simulationEngine;
    private final MitigationRequestTracker mitigationRequestTracker;

    public PlayerData(User user) {
        this.user = user;

        this.checkManager = new CheckManager(this);
        this.trigHandler = TrigHandler.VANILLA_MATH; // TODO: detect fast math or something idk
        this.transactionTracker = new TransactionTracker(this);
        this.positionTracker = new PositionTracker(this);
        this.rotationTracker = new RotationTracker(this);
        this.stateTracker = new StateTracker(this);
        this.usingTracker = new UsingTracker(this);
        this.actionTracker = new ActionTracker(this);
        this.attributeTracker = new AttributeTracker(this);
        this.potionTracker = new PotionTracker(this);
        this.inventoryTracker = new InventoryTracker(this);
        this.worldTracker = new WorldTracker(this);
        this.entityTracker = new EntityTracker(this);
        this.abilitiesTracker = new AbilitiesTracker(this);
        this.velocityTracker = new VelocityTracker(this);
        this.respawnTracker = new RespawnTracker(this);
        this.setbackTracker = new SetbackTracker(this);
        this.simulationEngine = new SimulationEngine(this);
        this.mitigationRequestTracker = new MitigationRequestTracker(this);
    }
}
