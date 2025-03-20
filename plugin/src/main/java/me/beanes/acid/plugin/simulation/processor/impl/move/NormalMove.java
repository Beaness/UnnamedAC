package me.beanes.acid.plugin.simulation.processor.impl.move;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import it.unimi.dsi.fastutil.floats.FloatArraySet;
import it.unimi.dsi.fastutil.floats.FloatCollection;
import it.unimi.dsi.fastutil.floats.FloatSet;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.util.MCMath;
import me.beanes.acid.plugin.util.splitstate.SplitState;

public class NormalMove extends MoveGetter {
    public NormalMove(PlayerData data) {
        super(data);
    }

    @Override
    public void addMovementFactors(FloatCollection factors, FloatCollection sprintFactors) {
        if (data.getPositionTracker().isLastOnGround()) {
            double[] moveSpeeds = getPossibleMoveSpeeds(false);
            double[] moveSpeedSprints = getPossibleMoveSpeeds(true);
            float[] allBlockSlipperiness = getAllBlockSlipperiness();

            for (float blockSlipperiness : allBlockSlipperiness) {
                float inertia = blockSlipperiness * 0.91F;

                data.getSimulationEngine().getCreator().getNormalInertia().add(inertia);

                for (double moveSpeed : moveSpeeds) {
                    float movementFactor = ((float)moveSpeed) * (0.16277136F / (inertia * inertia * inertia)); // Minecraft casts the move speed to float here

                    System.out.println("[Factor] normal=" + moveSpeed);

                    factors.add(movementFactor);
                }

                for (double moveSpeedSprint : moveSpeedSprints) {
                    float movementFactor = ((float)moveSpeedSprint) * (0.16277136F / (inertia * inertia * inertia)); // Minecraft casts the move speed to float here

                    System.out.println("[Factor] sprint=" + moveSpeedSprint);

                    sprintFactors.add(movementFactor);
                }
            }

        } else {
            factors.add(0.02F);

            // Air sprint speed is based on metadata, not attribute (thank god), also because of how air speed is set, you can sprint one tick longer backwards in the air
            if (data.getActionTracker().getClientSprintMetadata().possible() || data.getActionTracker().getLastClientSprintMetadata().possible()) {
                sprintFactors.add(0.025999999F);
            }

            data.getSimulationEngine().getCreator().getNormalInertia().add(0.91F);
        }
    }

    private float[] getAllBlockSlipperiness() {
        if (data.getPositionTracker().isLastUncertain()) {
            int minX = MCMath.floor_double(data.getPositionTracker().getLastX() - 0.03D);
            int minY = MCMath.floor_double(data.getPositionTracker().getLastY() - 1 - 0.03D);
            int minZ = MCMath.floor_double(data.getPositionTracker().getLastZ() - 0.03D);
            int maxX = MCMath.floor_double(data.getPositionTracker().getLastX() + 0.03D);
            int maxY = MCMath.floor_double(data.getPositionTracker().getLastY() - 1 + 0.03D);
            int maxZ = MCMath.floor_double(data.getPositionTracker().getLastZ() + 0.03D);

            FloatSet possible = new FloatArraySet(1);

            for (int x = minX; x <= maxX; ++x) {
                for (int y = minY; y <= maxY; ++y) {
                    for (int z = minZ; z <= maxZ; ++z) {
                        SplitState<WrappedBlockState> splitState = data.getWorldTracker().getBlock(x, y, z);

                        possible.add(getBlockSlipperiness(splitState.getValue().getType()));

                        if (splitState.getOldValue() != null) {
                            possible.add(getBlockSlipperiness(splitState.getOldValue().getType()));
                        }
                    }
                }
            }

            return possible.toFloatArray();
        } else {
            int x = MCMath.floor_double(data.getPositionTracker().getLastX());
            int y = MCMath.floor_double(data.getPositionTracker().getLastY() - 1);
            int z = MCMath.floor_double(data.getPositionTracker().getLastZ());

            SplitState<WrappedBlockState> splitState = data.getWorldTracker().getBlock(x, y, z);

            if (splitState.getOldValue() == null) {
                // We are certain about the block friction
                return new float[]{getBlockSlipperiness(splitState.getValue().getType())};
            } else {
                float value = getBlockSlipperiness(splitState.getValue().getType());
                float oldValue = getBlockSlipperiness(splitState.getOldValue().getType());

                // The 2 block split states could have the same friction
                if (value != oldValue) {
                    return new float[]{value, oldValue};
                } else {
                    return new float[]{value};
                }
            }
        }
    }

    public static float getBlockSlipperiness(StateType type) {
        if (type == StateTypes.ICE || type == StateTypes.PACKED_ICE) {
            return 0.98F;
        }

        if (type == StateTypes.SLIME_BLOCK) {
            return 0.8F;
        }

        return 0.6F;
    }
}
