package me.beanes.acid.plugin.player.tracker.impl.inventory;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import lombok.Getter;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.util.splitstate.ConfirmableState;


public class Container {
    @Getter
    private final int windowId;
    @Getter
    private final WindowType windowType;
    private final ConfirmableState<ItemStack>[] contents;

    public Container(int windowId, int slotCount, WindowType windowType) {
        this.windowId = windowId;
        this.windowType = windowType;
        //noinspection unchecked
        this.contents = new ConfirmableState[slotCount];

        for (int i = 0; i < contents.length; i++)
            contents[i] = new ConfirmableState<>(ItemStack.EMPTY);
    }

    public int getSlotCount() {
        return this.contents.length;
    }

    public ConfirmableState<ItemStack> getSlot(int slot) {
        return contents[slot];
    }

    public void checkTransaction(int slot, PlayerData data) {
        contents[slot].checkTransaction(data);
    }

    public void setSlot(int slot, ItemStack itemStack) {
        contents[slot].setValue(itemStack);
    }

    public void confirmSlot(int slot) {
        contents[slot].confirm();
    }

    // TODO: remove this
    public ConfirmableState<ItemStack>[] getContentsForDebug() {
        return contents;
    }

    public static Container createContainer(int id, int slots, String typeAsString) {
        WindowType type = WindowType.NORMAL;

        switch (typeAsString) {
            case "minecraft:chest":
            case "minecraft:container":
                slots = slots - (slots % 9); // Protect against dumb inventories that aren't a multiple of 9
                break;
            case "minecraft:dropper":
            case "minecraft:dispenser":
                slots = 9;
                break;
            case "minecraft:hopper":
                slots = 5;
                break;
            case "minecraft:furnace":
                type = WindowType.FURNACE;
                slots = 3;
                break;
            case "minecraft:brewing_stand":
                type = WindowType.BREWING_STAND;
                slots = 4;
                break;
            case "minecraft:beacon":
                type = WindowType.BEACON;
                slots = 1;
                break;
            case "minecraft:enchanting_table":
                type = WindowType.ENCHANTING_TABLE;
                slots = 2;
                break;
            case "minecraft:anvil":
                type = WindowType.ANVIL;
                slots = 3;
                break;
            case "minecraft:crafting_table":
                type = WindowType.CRAFTING_TABLE;
                slots = 10;
                break;
            case "minecraft:villager":
                type = WindowType.VILLAGER;
                slots = 3;
                break;
            case "EntityHorse":
                type = WindowType.HORSE;
                slots = 2;
                break;
            default:
                slots = 0;
                break;
        }

        return new Container(id, slots, type);
    }
}
