package me.beanes.acid.plugin.player.tracker.impl.inventory;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.ItemTags;

public class ContainerUtils {
    public static boolean isValidItemForSlot(Container container, int slot, ItemStack item) {
        // Enchanting tables only accept lapis in the second slot
        if (container.getWindowType().equals(WindowType.ENCHANTING_TABLE) && slot == 1 && !ItemUtils.isLapis(item)) {
            return false;
        }

        // Brewing stands slot 3 can only be interacted with if it is a brewing material
        if (container.getWindowType().equals(WindowType.BREWING_STAND) && slot == 3 && !ItemUtils.isBrewingMaterial(item.getType())) {
            return false;
        }

        // Brewing stands 3 slots can only be potions
        if (container.getWindowType().equals(WindowType.BREWING_STAND) && slot >= 0 && slot <= 2 && !ItemUtils.isBottle(item.getType())) {
            return false;
        }

        // You can't interact with horse sadle slot unless you have a sadle or there is already an item in it
        if (container.getWindowType().equals(WindowType.HORSE) && slot == 0 && item.getType() != ItemTypes.SADDLE) {
            return false;
        }

        // You can't interact with horse sadle slot unless you have a sadle or there is already a saddle in it
        if (container.getWindowType().equals(WindowType.HORSE) && slot == 1 && !ItemUtils.isHorseArmor(item.getType())) {
            return false;
        }

        // Furnace can only have fuel in slot 1
        if (container.getWindowType().equals(WindowType.FURNACE) && slot == 1 && !ItemUtils.isFurnaceFuel(item.getType(), true)) {
            return false;
        }

        // You can't interact with the anvil output slot if something is in your cursor
        if (container.getWindowType().equals(WindowType.ANVIL) && slot == 2) {
            return false;
        }

        // You are only able to interact with beacons if your cursor is an ingot
        if (container.getWindowType().equals(WindowType.BEACON) && slot == 0 && !ItemUtils.isBeaconIngot(item.getType())) {
            return false;
        }

        if (container.getWindowType().equals(WindowType.INVENTORY) && slot >= 5 && slot <= 8 && getArmorSlotForItem(item.getType(), true) != slot) {
            return false;
        }

        return true;
    }

    public static int getArmorSlotForItem(ItemType type, boolean customTypes) {
        if (ItemTags.HEAD_ARMOR.contains(type) || (customTypes && (ItemTags.SKULLS.contains(type) || type == ItemTypes.CARVED_PUMPKIN))) {
            return 5;
        }

        if (ItemTags.CHEST_ARMOR.contains(type)) {
            return 6;
        }

        if (ItemTags.LEG_ARMOR.contains(type)) {
            return 7;
        }

        if (ItemTags.FOOT_ARMOR.contains(type)) {
            return 8;
        }

        return Integer.MIN_VALUE;
    }
}
