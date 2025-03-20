package me.beanes.acid.plugin.simulation.processor.impl.move;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import it.unimi.dsi.fastutil.floats.*;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.util.splitstate.ConfirmableState;

public class WaterMove extends MoveGetter {
    public WaterMove(PlayerData data) {
        super(data);
    }

    @Override
    public void addMovementFactors(FloatCollection factors, FloatCollection sprintFactors) {
        FloatSet possibleDepthStrideModifiers = getPossibleDepthStrideModifiers();

        double[] moveSpeeds = getPossibleMoveSpeeds(false);
        double[] moveSpeedSprints = getPossibleMoveSpeeds(true);

        for (float enchantmentModifier : possibleDepthStrideModifiers) {
            float baseInertia = 0.8F;
            float baseMovementFactor = 0.02F;

            if (enchantmentModifier > 0.0F) {
                float inertia = baseInertia + ((0.54600006F - baseInertia) * enchantmentModifier / 3.0F);
                for (double moveSpeed : moveSpeeds) {
                    float movementFactor = baseMovementFactor + ((((float) moveSpeed) - baseMovementFactor) * enchantmentModifier / 3.0F);
                    factors.add(movementFactor);
                }

                for (double moveSpeed : moveSpeedSprints) {
                    float movementFactor = baseMovementFactor + ((((float) moveSpeed) - baseMovementFactor) * enchantmentModifier / 3.0F);
                    sprintFactors.add(movementFactor);
                }

                data.getSimulationEngine().getCreator().getWaterInertia().add(inertia);
            } else {
                factors.add(baseMovementFactor);
                data.getSimulationEngine().getCreator().getWaterInertia().add(baseInertia);
            }
        }

    }

    // Based on EnchantmentUtil but instantly does the depth strider calculation to less possibilities
    public FloatSet getPossibleDepthStrideModifiers() {
        FloatSet modifiers = new FloatArraySet();

        float certainMinimumLevel = 0.0F;

        for (int i = 5; i <= 8; i++) {
            ConfirmableState<ItemStack> state = data.getInventoryTracker().getInventoryContainer().getSlot(i);

            float latest = getDepthStriderEnchantment(state.getValue());
            float old = state.getOldValue() != null ? getDepthStriderEnchantment(state.getOldValue()) : latest;

            if (latest != old) {
                // We don't know which one of them is being taken in account for calculation
                modifiers.add(latest);
                modifiers.add(old);

                // We know the minimum value of these could up the min level
                certainMinimumLevel = Math.max(certainMinimumLevel, Math.min(latest, old));
            } else {
                // We know this could be a possible level
                modifiers.add(latest);

                // We know the latest is taken into account for possible, so let's update our certain max
                certainMinimumLevel = Math.max(certainMinimumLevel, latest);
            }
        }

        // Remove all possible modifiers below the certain minimum level we know
        FloatIterator iterator = modifiers.iterator();
        while (iterator.hasNext()) {
            if (iterator.nextFloat() < certainMinimumLevel) {
                iterator.remove();
            }
        }

        return modifiers;
    }

    public float getDepthStriderEnchantment(ItemStack itemStack) {
        float level = itemStack.getEnchantmentLevel(EnchantmentTypes.DEPTH_STRIDER, data.getUser().getClientVersion());

        if (level > 3.0F) {
            level = 3.0F;
        }

        if (!data.getPositionTracker().isLastOnGround()) {
            level *= 0.5F;
        }

        return level;
    }
}
