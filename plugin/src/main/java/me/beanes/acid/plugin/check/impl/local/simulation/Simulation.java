package me.beanes.acid.plugin.check.impl.local.simulation;

import me.beanes.acid.plugin.check.model.LocalCheck;
import me.beanes.acid.plugin.check.model.SimulationCheck;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.simulation.data.SimulationResult;
import org.bson.Document;
import org.bukkit.Bukkit;

public class Simulation extends LocalCheck implements SimulationCheck {

    public Simulation(PlayerData data) {
        super(data, "Simulation");
    }

    private long lastFlag = 0;


    @Override
    public void onSimulation(SimulationResult result) {
        if (!result.isCorrect()) {
            Document logData = new Document()
                    .append("normal", result.getLiquidState().isNormalPossible())
                    .append("water", result.getLiquidState().isWaterNotPossible())
                    .append("lava", result.getLiquidState().isLavaPossible())
                    .append("procesedVelocity", result.isProcessedVelocity())
                    .append("jump", result.getJumped());


            log(logData);

            debug("normal=" + result.getLiquidState().isNormalPossible() + " lava=" + result.getLiquidState().isLavaPossible() + " water=" + result.getLiquidState().isWaterPossible());

            neutralAlert("Simulation");

            // Resync close blocks
            if (System.currentTimeMillis() - lastFlag > 3_000) {
                this.lastFlag = System.currentTimeMillis();

                double x = data.getPositionTracker().getX();
                double y = data.getPositionTracker().getY();
                double z = data.getPositionTracker().getZ();
                data.getWorldTracker().getResyncHandler().scheduleResync(
                        (int) x + 3,
                        (int) y + 3,
                        (int) z + 3,
                        (int) x - 3,
                        (int) y - 3,
                        (int) z - 3
                );
            }
        }
    }
}
