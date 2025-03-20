package me.beanes.acid.plugin.simulation.processor.impl;

import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.simulation.data.MotionArea;
import me.beanes.acid.plugin.simulation.processor.AreaProcessor;
import me.beanes.acid.plugin.util.BoxUtil;
import me.beanes.acid.plugin.util.MCMath;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

public class LadderProcessor extends AreaProcessor {
    public LadderProcessor(PlayerData data) {
        super(data);
    }

    private SplitStateBoolean ladderState;

    public void prepareLadderState() {
        this.ladderState = getLadderState(true);
    }

    public void process(MotionArea area) {
        if (ladderState == SplitStateBoolean.TRUE) {
            area.minX = MCMath.clamp_double(area.minX, -0.15F, 0.15F);
            area.maxX = MCMath.clamp_double(area.maxX, -0.15F, 0.15F);
            area.minZ = MCMath.clamp_double(area.minZ, -0.15F, 0.15F);
            area.maxZ = MCMath.clamp_double(area.maxZ, -0.15F, 0.15F);

            if (data.getActionTracker().isSneaking()) {
                area.minY = Math.max(area.minY, 0.0D);
                area.maxY = Math.max(area.maxY, 0.0D);
            } else {
                area.minY = Math.max(area.minY, -0.15D);
                area.maxY = Math.max(area.maxY, -0.15D);
            }
        } else if (ladderState == SplitStateBoolean.POSSIBLE) {
            if (area.maxX > 0.15F) {
                area.minX = Math.min(area.minX, 0.15F);
            }

            if (area.minX < -0.15F) {
                area.maxX = Math.max(area.maxX, 0.15F);
            }

            if (area.maxZ > 0.15F) {
                area.minZ = Math.min(area.minZ, 0.15F);
            }

            if (area.minX < -0.15F) {
                area.maxZ = Math.max(area.maxZ, 0.15F);
            }

            if (data.getActionTracker().isSneaking()) {
                if (area.minY < 0.0D) {
                    area.maxY = Math.max(area.maxY, 0.0D);
                }
            } else {
                if (area.minY < -0.15D) {
                    area.maxY = Math.max(area.maxY, -0.15D);
                }
            }
        }
    }

    // We need a last boolean to check the ladder state after the simulation, this is needed to create the next motion area (motionY is set to 0.2 if still on ladder)
    public SplitStateBoolean getLadderState(boolean last) {
        double x, y, z;

        x = last ? data.getPositionTracker().getLastReportedX() : data.getPositionTracker().getReportedX();
        y = last ? data.getPositionTracker().getLastReportedY() : data.getPositionTracker().getReportedY();
        z = last ? data.getPositionTracker().getLastReportedZ() : data.getPositionTracker().getReportedZ();

        // The position before was uncertain so we don't know from where the player started his motion
        if (last ? data.getPositionTracker().isLastUncertain() : data.getPositionTracker().isUncertain()) {
            BoundingBox uncertainBox = new BoundingBox(
                    x - (BoxUtil.PLAYER_WIDTH / 2.0F) - 0.03D,
                    y - 0.03D,
                    z - (BoxUtil.PLAYER_WIDTH / 2.0F) - 0.03D,
                    x + (BoxUtil.PLAYER_WIDTH / 2.0F) + 0.03D,
                    y + 0.03D,
                    z + (BoxUtil.PLAYER_WIDTH / 2.0F) + 0.03D
            );

            SplitStateBoolean materialInBB = data.getWorldTracker().isMaterialInBB(uncertainBox, StateTypes.LADDER, StateTypes.VINE);

            // if the material is in the bounding box or might be, it is a possible ladder true state
            return (materialInBB == SplitStateBoolean.TRUE || materialInBB == SplitStateBoolean.POSSIBLE) ? SplitStateBoolean.POSSIBLE : SplitStateBoolean.FALSE;
        } else {
            int blockX = MCMath.floor_double(x);
            int blockY = MCMath.floor_double(y);
            int blockZ = MCMath.floor_double(z);

            SplitStateBoolean ladderState = data.getWorldTracker().isMaterial(blockX, blockY, blockZ, StateTypes.LADDER);
            SplitStateBoolean vineState = data.getWorldTracker().isMaterial(blockX, blockY, blockZ, StateTypes.VINE);

            if (ladderState == SplitStateBoolean.TRUE || vineState == SplitStateBoolean.TRUE) {
                // If the state is certain but we might have been in liquid then we aren't 100% sure the ladder state was true
                if (data.getSimulationEngine().getLiquidState().isWaterPossible() || data.getSimulationEngine().getLiquidState().isLavaPossible()) {
                    return SplitStateBoolean.POSSIBLE;
                }

                return SplitStateBoolean.TRUE;
            }

            // Return the most possible state (if ladder possible -> return ladder, otherwise return vine as that could be possible)
            return ladderState == SplitStateBoolean.POSSIBLE ? ladderState : vineState;
        }
    }
}
