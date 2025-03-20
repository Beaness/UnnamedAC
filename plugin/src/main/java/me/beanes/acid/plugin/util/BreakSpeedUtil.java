package me.beanes.acid.plugin.util;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.world.MaterialType;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.ItemTags;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.floats.FloatArraySet;
import it.unimi.dsi.fastutil.floats.FloatSet;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.simulation.prepare.WaterFlow;
import me.beanes.acid.plugin.util.splitstate.ConfirmableState;
import me.beanes.acid.plugin.util.splitstate.SplitState;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

import java.util.Set;

public class BreakSpeedUtil {
    // Returns all possible breakspeeds
    public static FloatSet getBreakSpeeds(PlayerData data, StateType type) {
        FloatSet breakSpeeds = new FloatArraySet(1);
        float blockHardness = type.getHardness();

        if (blockHardness < 0.0F) {
            breakSpeeds.add(0.0F);
        } else {
            for (float correctToolModifier : getCorrectToolModifiers(data, type)) {
                for (float digEfficiency : getToolDigEfficiency(data, type)) {
                    breakSpeeds.add(digEfficiency / blockHardness / correctToolModifier);
                }
            }
        }

        if (data.getStateTracker().isCreative() == SplitStateBoolean.POSSIBLE) {
            breakSpeeds.add(1.0F);
        }


        return breakSpeeds;
    }

    private static float[] getCorrectToolModifiers(PlayerData data, StateType type) {
        if (!type.isRequiresCorrectTool()) {
            return new float[]{30.0F};
        }

        ConfirmableState<ItemStack> itemSplitState = data.getInventoryTracker().getHeldItem();
        ItemType latest = itemSplitState.getValue().getType();
        ItemType old = itemSplitState.getOldValue() != null ? itemSplitState.getOldValue().getType() : null;

        boolean correct = false;
        boolean oldCorrect = correct;

        if (type == StateTypes.COBWEB) {
            correct = ItemTags.SWORDS.contains(latest) || latest == ItemTypes.SHEARS;
            oldCorrect = old != null ? ItemTags.SWORDS.contains(old) || old == ItemTypes.SHEARS : correct;
        } else if (type == StateTypes.SNOW) {
            correct = ItemTags.SHOVELS.contains(latest);
            oldCorrect = old != null ? ItemTags.SHOVELS.contains(old) : correct;
        } else if (type == StateTypes.REDSTONE_WIRE || type == StateTypes.TRIPWIRE_HOOK) {
            correct = latest == ItemTypes.SHEARS;
            oldCorrect = old != null ? old == ItemTypes.SHEARS : correct;
        } else if (type == StateTypes.OBSIDIAN) {
            correct = latest == ItemTypes.DIAMOND_PICKAXE;
            oldCorrect = old != null ? old == ItemTypes.DIAMOND_PICKAXE : correct;
        } else if (type == StateTypes.DIAMOND_BLOCK || type == StateTypes.DIAMOND_ORE
                || type == StateTypes.EMERALD_BLOCK || type == StateTypes.EMERALD_ORE
                || type == StateTypes.GOLD_BLOCK || type == StateTypes.GOLD_ORE
                || type == StateTypes.REDSTONE_ORE
        ) {
            correct = latest == ItemTypes.DIAMOND_PICKAXE || latest == ItemTypes.IRON_PICKAXE;
            oldCorrect = old != null ? old == ItemTypes.DIAMOND_PICKAXE || old == ItemTypes.IRON_PICKAXE : correct;
        } else if (type == StateTypes.IRON_BLOCK || type == StateTypes.IRON_ORE
                || type == StateTypes.LAPIS_BLOCK || type == StateTypes.LAPIS_ORE) {
            correct = latest == ItemTypes.DIAMOND_PICKAXE || latest == ItemTypes.IRON_PICKAXE || latest == ItemTypes.STONE_PICKAXE;
            oldCorrect = old != null ? old == ItemTypes.DIAMOND_PICKAXE || old == ItemTypes.IRON_PICKAXE || old == ItemTypes.STONE_PICKAXE : correct;
        } else if (type.getMaterialType() == MaterialType.STONE || type.getMaterialType() == MaterialType.METAL) {
            correct = ItemTags.PICKAXES.contains(latest);
            oldCorrect = old != null ? ItemTags.PICKAXES.contains(old) : correct;
        }

        SplitStateBoolean result = SplitStateBoolean.result(correct, oldCorrect);

        if (result == SplitStateBoolean.FALSE) {
            return new float[]{100.0F};
        } else if (result == SplitStateBoolean.TRUE) {
            return new float[]{30.0F};
        } else {
            return new float[]{30.0F, 100.0F};
        }
    }

    private static FloatSet getToolDigEfficiency(PlayerData data, StateType type) {
        float[] strengths = getStrengths(data, type);

        FloatSet afterEfficiency = new FloatArraySet();
        int[] efficiencyLevels = getEfficiencyLevel(data);
        for (float base : strengths) {
            if (base > 1.0F) {
                for (int efficiency : efficiencyLevels) {
                    if (efficiency > 0) {
                        afterEfficiency.add(base + (float)(efficiency * efficiency + 1));
                    } else {
                        afterEfficiency.add(base);
                    }
                }
            } else {
                afterEfficiency.add(base);
            }
        }

        FloatSet afterHaste = new FloatArraySet(afterEfficiency.size());
        for (int amplifier : getHaste(data)) {
            for (float base : afterEfficiency) {
                if (amplifier > 0) {
                    afterHaste.add(base * (1.0F + (amplifier * 0.2F)));
                } else {
                    afterHaste.add(base);
                }
            }
        }

        FloatSet afterMiningFatigue = new FloatArraySet(afterHaste.size());
        for (int amplifier : getMiningFatigue(data)) {
            for (float base : afterHaste) {
                float modifier;
                switch (amplifier) {
                    case 0:
                        modifier = 1.0F;
                        break;

                    case 1:
                        modifier = 0.3F;
                        break;

                    case 2:
                        modifier = 0.09F;
                        break;

                    case 3:
                        modifier = 0.0027F;
                        break;

                    case 4:
                    default:
                        modifier = 8.1E-4F;
                }

                afterMiningFatigue.add(base * modifier);
            }
        }

        FloatSet afterWater = new FloatArraySet(afterMiningFatigue.size());
        SplitStateBoolean waterLimited = isWaterLimited(data);
        for (float base : afterMiningFatigue) {
            if (waterLimited == SplitStateBoolean.TRUE) {
                afterWater.add(base / 5.0F);
            } else if (waterLimited == SplitStateBoolean.POSSIBLE) {
                afterWater.add(base / 5.0F);
                afterWater.add(base);
            } else {
                afterWater.add(base);
            }
        }

        FloatSet result = new FloatArraySet(afterWater.size());
        boolean onGround = data.getPositionTracker().isOnGround();
        for (float base : afterWater) {
            if (!onGround) {
                result.add(base / 5.0F);
            } else {
                result.add(base);
            }
        }


        return result;
    }

    private static float[] getStrengths(PlayerData data, StateType type) {

        ConfirmableState<ItemStack> itemSplitState = data.getInventoryTracker().getHeldItem();

        float latest = getStrength(itemSplitState.getValue().getType(), type);
        float old = itemSplitState.getOldValue() != null ? getStrength(itemSplitState.getOldValue().getType(), type) : latest;

        if (latest != old) {
            return new float[]{latest, old};
        } else {
            return new float[]{latest};
        }
    }

    private static final Set<StateType> AXE_EFFECTIVE_ON = Sets.newHashSet(StateTypes.BOOKSHELF, StateTypes.CHEST, StateTypes.PUMPKIN, StateTypes.JACK_O_LANTERN, StateTypes.MELON, StateTypes.LADDER);
    private static final Set<StateType> PICKAXE_EFFECTIVE_ON = Sets.newHashSet(StateTypes.ACTIVATOR_RAIL, StateTypes.COAL_ORE, StateTypes.COBBLESTONE, StateTypes.DETECTOR_RAIL, StateTypes.DIAMOND_BLOCK, StateTypes.DIAMOND_ORE, StateTypes.STONE_SLAB, StateTypes.POWERED_RAIL, StateTypes.GOLD_BLOCK, StateTypes.GOLD_ORE, StateTypes.ICE, StateTypes.IRON_BLOCK, StateTypes.IRON_ORE, StateTypes.LAPIS_BLOCK, StateTypes.LAPIS_ORE, StateTypes.MOSSY_COBBLESTONE, StateTypes.NETHERRACK, StateTypes.PACKED_ICE, StateTypes.RAIL, StateTypes.REDSTONE_ORE, StateTypes.RED_SANDSTONE, StateTypes.RED_SANDSTONE, StateTypes.STONE, StateTypes.STONE_SLAB);
    private static final Set<StateType> SHOVEL_EFFECTIVE_ON = Sets.newHashSet(StateTypes.CLAY, StateTypes.DIRT, StateTypes.FARMLAND, StateTypes.GRASS_BLOCK, StateTypes.GRAVEL, StateTypes.MYCELIUM, StateTypes.SAND, StateTypes.SNOW_BLOCK, StateTypes.SNOW, StateTypes.SOUL_SAND);

    public static float getStrength(ItemType item, StateType type) {
        if (ItemTags.SWORDS.contains(item)) {
            if (type == StateTypes.COBWEB) {
                return 15.0F;
            } else if (type.getMaterialType() == MaterialType.PLANT || type.getMaterialType() == MaterialType.REPLACEABLE_PLANT || type.getMaterialType() == MaterialType.LEAVES || type.getMaterialType() == MaterialType.VEGETABLE) {
                return 1.5F;
            }
        } else if (item == ItemTypes.SHEARS) {
            if (type == StateTypes.COBWEB || type.getMaterialType() == MaterialType.LEAVES) {
                return 15.0F;
            }

            if (BlockTags.WOOL.contains(type)) {
                return 5.0F;
            }
        } else if (ItemTags.AXES.contains(item)) {
            if (type.getMaterialType() == MaterialType.WOOD || type.getMaterialType() == MaterialType.PLANT || type.getMaterialType() == MaterialType.REPLACEABLE_PLANT) {
                return getEfficiencyForProperMaterial(item);
            }

            if (AXE_EFFECTIVE_ON.contains(type)) {
                return getEfficiencyForProperMaterial(item);
            }
        } else if (ItemTags.PICKAXES.contains(item)) {
            if (type.getMaterialType() == MaterialType.METAL || type.getMaterialType() == MaterialType.HEAVY_METAL || type.getMaterialType() == MaterialType.STONE) {
                return getEfficiencyForProperMaterial(item);
            }

            if (PICKAXE_EFFECTIVE_ON.contains(type)) {
                return getEfficiencyForProperMaterial(item);
            }
        } else if (ItemTags.SHOVELS.contains(item)) {
            if (SHOVEL_EFFECTIVE_ON.contains(type)) {
                return getEfficiencyForProperMaterial(item);
            }
        }

        return 1.0F;
    }

    private static float getEfficiencyForProperMaterial(ItemType type) {
        if (type.hasAttribute(ItemTypes.ItemAttribute.WOOD_TIER)) {
            return 2.0F;
        } else if (type.hasAttribute(ItemTypes.ItemAttribute.STONE_TIER)) {
            return 4.0F;
        } else if (type.hasAttribute(ItemTypes.ItemAttribute.IRON_TIER)) {
            return 6.0F;
        } else if (type.hasAttribute(ItemTypes.ItemAttribute.DIAMOND_TIER)) {
            return 8.0F;
        } else if (type.hasAttribute(ItemTypes.ItemAttribute.GOLD_TIER)) {
            return 12.0F;
        }

        return 4.0F;
    }

    private static int[] getEfficiencyLevel(PlayerData data) {
        ConfirmableState<ItemStack> itemSplitState = data.getInventoryTracker().getHeldItem();

        int latest = itemSplitState.getValue().getEnchantmentLevel(EnchantmentTypes.BLOCK_EFFICIENCY, data.getUser().getClientVersion());
        int old = itemSplitState.getOldValue() != null ? itemSplitState.getOldValue().getEnchantmentLevel(EnchantmentTypes.BLOCK_EFFICIENCY, data.getUser().getClientVersion()) : latest;

        if (latest != old) {
            return new int[]{latest, old};
        } else {
            return new int[]{latest};
        }
    }

    private static int[] getHaste(PlayerData data) {
        int latest = data.getPotionTracker().getHasteAmplifier().getValue();
        int old = data.getPotionTracker().getHasteAmplifier().getOldValue() != null ? data.getPotionTracker().getHasteAmplifier().getOldValue() : latest;

        if (latest != old) {
            return new int[]{latest, old};
        } else {
            return new int[]{latest};
        }
    }

    private static int[] getMiningFatigue(PlayerData data) {
        int latest = data.getPotionTracker().getMiningFatigueAmplifier().getValue();
        int old = data.getPotionTracker().getMiningFatigueAmplifier().getOldValue() != null ? data.getPotionTracker().getMiningFatigueAmplifier().getOldValue() : latest;

        if (latest != old) {
            return new int[]{latest, old};
        } else {
            return new int[]{latest};
        }
    }

    private static SplitStateBoolean isWaterLimited(PlayerData data) {
        SplitStateBoolean aquaInfinity = hasAquaAffinity(data);

        if (aquaInfinity == SplitStateBoolean.TRUE) {
            return SplitStateBoolean.FALSE;
        }

        SplitStateBoolean insideWater = isInsideOfWater(data);

        if (insideWater == SplitStateBoolean.FALSE) {
            return SplitStateBoolean.FALSE;
        } else if (insideWater == SplitStateBoolean.TRUE) {
            if (aquaInfinity == SplitStateBoolean.POSSIBLE) {
                return SplitStateBoolean.POSSIBLE;
            }

            return SplitStateBoolean.TRUE;
        }

        return SplitStateBoolean.POSSIBLE;
    }

    public static SplitStateBoolean hasAquaAffinity(PlayerData data) {
        boolean possible = false;

        for (int i = 5; i <= 8; i++) {
            ConfirmableState<ItemStack> state = data.getInventoryTracker().getInventoryContainer().getSlot(i);

            boolean latest = state.getValue().getEnchantmentLevel(EnchantmentTypes.AQUA_AFFINITY, data.getUser().getClientVersion()) > 0;
            boolean old = state.getOldValue() != null ? state.getOldValue().getEnchantmentLevel(EnchantmentTypes.AQUA_AFFINITY, data.getUser().getClientVersion()) > 0 : latest;

            if (latest != old) {
                possible = true;
            } else if (latest) {
                return SplitStateBoolean.TRUE;
            }
        }

        return possible ? SplitStateBoolean.POSSIBLE : SplitStateBoolean.FALSE;
    }

    public static SplitStateBoolean isInsideOfWater(PlayerData data) {
        double lookY = data.getPositionTracker().getY() + (data.getActionTracker().isSneaking() ? (double) (1.62F - 0.08F) : (double)1.62f);
        int x = MCMath.floor_double(data.getPositionTracker().getX());
        int y = MCMath.floor_double(lookY);
        int z = MCMath.floor_double(data.getPositionTracker().getZ());

        SplitState<WrappedBlockState> splitState = data.getWorldTracker().getBlock(x, y, z);
        boolean latest = isInsideWater(lookY, y, splitState.getValue());
        boolean old = splitState.getOldValue() != null ? isInsideWater(lookY, y, splitState.getOldValue()) : latest;

        return SplitStateBoolean.result(latest, old);
    }

    private static boolean isInsideWater(double lookY, int floorY, WrappedBlockState state) {
        if (state.getType() == StateTypes.WATER) {
            float f = WaterFlow.getLiquidHeightPercent(state.getLevel()) - 0.11111111F;
            float f1 = (float) (floorY + 1) - f;
            return lookY < (double) f1;
        } else {
            return false;
        }
    }
}
