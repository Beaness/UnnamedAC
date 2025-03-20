package me.beanes.acid.plugin.simulation.processor.impl;

import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.simulation.data.MotionArea;
import me.beanes.acid.plugin.simulation.processor.AreaProcessor;
import me.beanes.acid.plugin.util.MCMath;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

public class JumpProcessor extends AreaProcessor {
    public JumpProcessor(PlayerData data) {
        super(data);
    }

    /*
        The jump processor calculates a new Area which includes all possible jump motions & liquid jumps
        This splits the starting area basically into two areas (read the code in SimulationEngine.java)
        - A jump area
        - A no jump area

        We have to split because if we allow no jump & jump in the same area we have a low bhop bypass
        We could technically also increase minY if we are certain but that has a lot of edge cases, its easier to split into 2 areas

        Also another reason to have another area for jumping is to detect player jumps
        This allows us to make a heuristic check to detect jump reset velocity
     */

    public void process(MotionArea area) {
        double originalMaxY = area.maxY;

        if (data.getSimulationEngine().getLiquidState().isNormalPossible()) {
            double extraJump = (float) (data.getPotionTracker().getJumpBoostAmplifier().getValue()) * 0.1F;

            area.minY = ((double) 0.42F) + extraJump;
            area.maxY = ((double) 0.42F) + extraJump;

            // Extends the area with the old value if needed
            if (data.getPotionTracker().getJumpBoostAmplifier().getOldValue() != null) {
                double extraJumpOld = (float) (data.getPotionTracker().getJumpBoostAmplifier().getOldValue()) * 0.1F;
                area.allowY(((double) 0.42F) + extraJumpOld);
            }

            float rad = data.getRotationTracker().getYaw() * MCMath.DEGREES_TO_RADIANS;

            if (data.getSimulationEngine().getLiquidState().isNormalCertain() && data.getActionTracker().getClientSprintMetadata() == SplitStateBoolean.TRUE) {
                // Move the area with the jump motion if no liquid is possible
                area.minX -= data.getTrigHandler().sin(rad) * 0.2F;
                area.maxX -= data.getTrigHandler().sin(rad) * 0.2F;
                area.minZ += data.getTrigHandler().cos(rad) * 0.2F;
                area.maxZ += data.getTrigHandler().cos(rad) * 0.2F;
            } else if (data.getActionTracker().getClientSprintMetadata().possible()) { // Could be sprint=TRUE & water/lava or sprint=POSSIBLE
                // Its possible the player was in a liquid or not sprinting and the normal jump code was never ran, extend the area instead of moving it
                double additionalX = -(data.getTrigHandler().sin(rad) * 0.2F);
                double additionalZ = data.getTrigHandler().cos(rad) * 0.2F;

                area.extendX(additionalX);
                area.extendZ(additionalZ);
            }
        }

        if (data.getSimulationEngine().getLiquidState().isWaterPossible() || data.getSimulationEngine().getLiquidState().isLavaPossible()) {
            if (data.getSimulationEngine().getLiquidState().isNormalPossible()) {
                // The liquid code jump might not have been ran, extend the area if needed
                area.allowY(originalMaxY + (double) 0.04F);
            } else {
                // The liquid jump code definitely ran
                area.minY = area.minY + (double) 0.04F;
                area.maxY = area.maxY + (double) 0.04F;
            }
        }
    }

    /*
        A jump is only possible in a few easily seen scenarios
            - The player is in a liquid
            - The player old ground state is true and the next ground state is false
            - The player old ground state is true and the next ground state is also true if the player has a negative jump aplifier
     */

    public boolean wasJumpPossible() {
        if (data.getSimulationEngine().getLiquidState().isWaterPossible() || data.getSimulationEngine().getLiquidState().isLavaPossible()) {
            return true;
        }

        if (data.getSimulationEngine().getLiquidState().isNormalPossible()) {
            boolean possibleNegativeJump = data.getPotionTracker().getJumpBoostAmplifier().getValue() < 0 || (data.getPotionTracker().getJumpBoostAmplifier().getOldValue() != null && data.getPotionTracker().getJumpBoostAmplifier().getOldValue() < 0);
            return data.getPositionTracker().isLastOnGround() // You can only jump from the ground
                    && (!data.getPositionTracker().isOnGround() || possibleNegativeJump); // New state has to be either not on ground or a negative jump
        }

        return false;
    }
}
