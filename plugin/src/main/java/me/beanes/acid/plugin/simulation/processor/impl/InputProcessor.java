package me.beanes.acid.plugin.simulation.processor.impl;

import it.unimi.dsi.fastutil.floats.*;
import me.beanes.acid.plugin.simulation.data.ForwardStrafe;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.simulation.data.MotionArea;
import me.beanes.acid.plugin.simulation.processor.AreaProcessor;
import me.beanes.acid.plugin.simulation.processor.impl.move.LavaMove;
import me.beanes.acid.plugin.simulation.processor.impl.move.MoveGetter;
import me.beanes.acid.plugin.simulation.processor.impl.move.NormalMove;
import me.beanes.acid.plugin.simulation.processor.impl.move.WaterMove;

import java.util.ArrayList;
import java.util.List;

// Does not extend AreaProcessor because this splits one area into multiple areas based on input
public class InputProcessor extends AreaProcessor {
    private final MoveGetter normalMove, lavaMove, waterMove;

    public InputProcessor(PlayerData data) {
        super(data);
        this.normalMove = new NormalMove(data);
        this.lavaMove = new LavaMove(data);
        this.waterMove = new WaterMove(data);
    }

    private ForwardStrafe[] forwardStrafes;
    private final FloatSet factors = new FloatArraySet();
    private final FloatSet factorsWithSprint = new FloatArraySet();

    public void prepareInputMovement() {
        this.forwardStrafes = ForwardStrafe.get(data.getActionTracker().isSneaking(), data.getUsingTracker().getUsing());

        factors.clear();
        factorsWithSprint.clear();

        if (data.getSimulationEngine().getLiquidState().isNormalPossible()) {
            normalMove.addMovementFactors(factors, factorsWithSprint);
        }

        if (data.getSimulationEngine().getLiquidState().isLavaPossible()) {
            lavaMove.addMovementFactors(factors, factorsWithSprint);
        }

        if (data.getSimulationEngine().getLiquidState().isWaterPossible()) {
            waterMove.addMovementFactors(factors, factorsWithSprint);
        }

        factorsWithSprint.addAll(factors); // Add the normal factors the with sprint collection
    }

    public List<MotionArea> process(MotionArea area) {
        float yawInRadians = data.getRotationTracker().getYaw() * (float)Math.PI / 180.0F;
        float sinYaw = data.getTrigHandler().sin(yawInRadians);
        float cosYaw = data.getTrigHandler().cos(yawInRadians);

        List<MotionArea> areas = new ArrayList<>();
        areas.add(area); // Add back the normal area as a "no input" area

        int maxFoodLevel = Math.max(data.getStateTracker().getFood().getValue(), data.getStateTracker().getFood().getOldValue() != null ? data.getStateTracker().getFood().getOldValue() : data.getStateTracker().getFood().getValue());
        boolean possibleFlyingAllowed = data.getAbilitiesTracker().getFlightAllowed().getValue() | (data.getAbilitiesTracker().getFlightAllowed().getOldValue() != null ? data.getAbilitiesTracker().getFlightAllowed().getOldValue() : data.getAbilitiesTracker().getFlightAllowed().getValue());

        boolean noFoodAllowed = ((float)maxFoodLevel) < 6.0F && !possibleFlyingAllowed;

        for (ForwardStrafe forwardStrafe : forwardStrafes) {
            // No sprint is only for ground movement
            // As you can sprint backwards in air for a tick
            // This code is questionable (its a bit funky but trust me stuff works and it makes sense if you look at the internals)
            boolean noSprint = false;

            if (data.getPositionTracker().isLastOnGround()) {
                if (noFoodAllowed) {
                    noSprint = true;
                } else if (forwardStrafe.getForward() < 0.8F) {
                    noSprint = true;
                }
            }

            // We are not sure if the sprint was cancelled due to the server setting the sprint in entity bitmask to false which can allow u to omnisprint
            if (data.getActionTracker().isUncertainSprintAttribute()) {
                noSprint = false;
            }



            System.out.println("noSprint=" + noSprint + " serverOverride=" + data.getActionTracker().isPossibleOverride() + " srvOverride=" + data.getActionTracker().isServerSprintState() + " sprint=" + data.getActionTracker().getClientSprintMetadata());

            for (float movementFactor : noSprint ? factors : factorsWithSprint) {
                // Only under these specific conditions do you get a distance improvement:
                float distance = (forwardStrafe.getForward() != 0 && forwardStrafe.getStrafe() != 0 && !forwardStrafe.isSneaking() && !forwardStrafe.isUsing()) ? 1.3859293F : 1.0F; //  (1.3859293F is the calculated result)

                distance = movementFactor / distance;

                System.out.println("forward=" + forwardStrafe.getForward() + " strafe=" + forwardStrafe.getStrafe());

                float strafe = forwardStrafe.getStrafe() * distance;
                float forward = forwardStrafe.getForward() * distance;

                double x = strafe * cosYaw - forward * sinYaw;
                double z = forward * cosYaw + strafe * sinYaw;

                MotionArea areaWithInput = area.copy();

                // This is needed for dumb mojang netcode to prevent no slow based on next tick
                areaWithInput.using = forwardStrafe.isUsing();

                System.out.println("movementFactor=" + movementFactor + " x=" + x + " z=" + z  + " strafe=" + strafe + " forward=" + forward + "yaw= " + data.getRotationTracker().getYaw() + " area=" + areaWithInput.getTrack());

                // Move the area with the input
                areaWithInput.minX += x;
                areaWithInput.maxX += x;
                areaWithInput.minZ += z;
                areaWithInput.maxZ += z;

                areas.add(areaWithInput);
            }
        }

        return areas;
    }

}
