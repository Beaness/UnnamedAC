package me.beanes.acid.plugin.player.tracker.impl.inventory;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.ItemTags;

public class ItemUtils {
    public static boolean isItemEqual(ItemStack first, ItemStack other) {
        return first.getType() == other.getType() && first.getLegacyData() == other.getLegacyData();
    }

    public static boolean isNBTEqual(ItemStack first, ItemStack other) {
        return (first.getNBT() == null && other.getNBT() == null) || (first.getNBT() != null && first.getNBT().equals(other.getNBT()));
    }

    public static boolean isEquivalent(ItemStack first, ItemStack other) {
        return isItemEqual(first, other) && isNBTEqual(first, other);
    }

    public static boolean canAddItem(ItemStack in, ItemStack add) {
        if (in == ItemStack.EMPTY) {
            return true;
        }

        if (isEquivalent(in, add)) {
            if (add.getAmount() < 1) {
                return true;
            }

            return in.getAmount() < in.getType().getMaxAmount();
        }

        return false;
    }

    public static boolean isLapis(ItemStack item) {
        return item.getType() == ItemTypes.INK_SAC && item.getLegacyData() == 4;
    }

    public static boolean isFurnaceFuel(ItemType type, boolean bucketCounts) {
        if (type == ItemTypes.BUCKET) {
            return bucketCounts;
        }

        return type.hasAttribute(ItemTypes.ItemAttribute.FUEL);
    }

    public static boolean isBrewingMaterial(ItemType type) {
        return type == ItemTypes.GUNPOWDER || type == ItemTypes.REDSTONE
                || type == ItemTypes.GLOWSTONE_DUST || type == ItemTypes.SUGAR
                || type == ItemTypes.GHAST_TEAR || type == ItemTypes.NETHER_WART
                || type == ItemTypes.SPIDER_EYE || type == ItemTypes.FERMENTED_SPIDER_EYE
                || type == ItemTypes.BLAZE_POWDER || type == ItemTypes.MAGMA_CREAM
                || type == ItemTypes.GLISTERING_MELON_SLICE || type == ItemTypes.GOLDEN_CARROT
                || type == ItemTypes.RABBIT_FOOT;
    }

    public static boolean isBottle(ItemType type) {
        return type == ItemTypes.POTION || type == ItemTypes.GLASS_BOTTLE;
    }

    public static boolean isHorseArmor(ItemType type) {
        return type == ItemTypes.IRON_HORSE_ARMOR || type == ItemTypes.GOLDEN_HORSE_ARMOR || type == ItemTypes.DIAMOND_HORSE_ARMOR;
    }

    public static boolean isBeaconIngot(ItemType type) {
        return type == ItemTypes.DIAMOND || type == ItemTypes.IRON_INGOT || type == ItemTypes.GOLD_INGOT || type == ItemTypes.EMERALD;
    }

    public static boolean isArmor(ItemType type) {
        return ItemTags.HEAD_ARMOR.contains(type) || ItemTags.CHEST_ARMOR.contains(type) || ItemTags.LEG_ARMOR.contains(type) || ItemTags.FOOT_ARMOR.contains(type);
    }
}
