package me.beanes.acid.plugin.player.tracker.impl.inventory;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCreativeInventoryAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientHeldItemChange;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import lombok.Getter;
import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.player.tracker.Tracker;
import me.beanes.acid.plugin.util.MCMath;
import me.beanes.acid.plugin.util.splitstate.ConfirmableState;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow.WindowClickType;

/*
    This whole class file is kind of a mess
    I tried my best but uhh... yeah..  (why did i make this :()
    I didn't wanna spend my time copying MCP (which is barely readable!) so I just made my own implementation of inventory tracking with (semi) split state support (need to work on this..)
    A lot of code isnt fully correct, but it hangs.. somewhat together I guess

    should actually delete this for my mental health ~ Beanes
 */


public class InventoryTracker extends Tracker {
    public InventoryTracker(PlayerData data) {
        super(data);
    }
    @Getter
    private final Container inventoryContainer = new Container(0, 45, WindowType.INVENTORY);
    private final ConfirmableState<Container> openContainer = new ConfirmableState<>(inventoryContainer);
    private final ConfirmableState<ItemStack> cursorItemStack = new ConfirmableState<>(ItemStack.EMPTY);
    private DragType dragMode = DragType.NONE;
    private final Set<DragSlot> dragSlots = new ObjectArraySet<>();
    @Getter
    private int heldSlot = 0; // The one desync could be when a player switches server

    public ConfirmableState<ItemStack> getHeldItem() {
        return inventoryContainer.getSlot(36 + heldSlot);
    }

    public void removeOneItemFromHeldItemIfNotCreative() {
        if (data.getStateTracker().isCreative().notPossible()) {
            ConfirmableState<ItemStack> confirmableState = getHeldItem();
            ItemStack stack = confirmableState.getValue();

            stack.setAmount(stack.getAmount() - 1);

            if (stack.getAmount() <= 0) {
                confirmableState.setValueCertainly(ItemStack.EMPTY);
            }

            if (data.getStateTracker().isCreative() == SplitStateBoolean.POSSIBLE) {
                attemptResync();
            }
        }
    }

    private long lastResync = System.currentTimeMillis();

    public void attemptResync() {
        UUID uuid = data.getUser().getUUID();

        if (System.currentTimeMillis() - lastResync > 1_000) {
            lastResync = System.currentTimeMillis();
            Bukkit.getScheduler().runTaskLater(Acid.get(), () -> {
                Bukkit.getPlayer(uuid).updateInventory();
            }, 1L);
        }
    }

    public void handleOpenWindow(WrapperPlayServerOpenWindow wrapper) {
        openContainer.checkTransaction(data);

        Container newContainer = Container.createContainer(wrapper.getContainerId(), wrapper.getLegacySlots(), wrapper.getLegacyType());

        data.getTransactionTracker().pre(() -> {
            openContainer.setValue(newContainer);
        });

        data.getTransactionTracker().post(openContainer::confirm);
    }

    public void handleServerCloseWindow() {
        openContainer.checkTransaction(data);
        cursorItemStack.checkTransaction(data);

        data.getTransactionTracker().pre(() -> {
            cursorItemStack.setValue(ItemStack.EMPTY);
            openContainer.setValue(inventoryContainer);
        });

        data.getTransactionTracker().post(() -> {
            cursorItemStack.confirm();
            openContainer.confirm();
        });
    }

    private void checkTransactionsForWindowItems(Container container, int checkLength, boolean skipInventory) {
        for (int i = 0; i < checkLength; i++) {
            if (i >= container.getSlotCount()) {
                if (skipInventory) break;

                inventoryContainer.checkTransaction(getInventoryOffset(container.getSlotCount(), i), data);
            } else {
                container.checkTransaction(i, data);
            }
        }
    }

    private void setItemsForWindowItems(Container container, List<ItemStack> items, boolean skipInventory) {
        for (int i = 0; i < items.size(); i++) {
            if (i >= container.getSlotCount()) {
                if (skipInventory) break;

                inventoryContainer.setSlot(getInventoryOffset(container.getSlotCount(), i), items.get(i));
            } else {
                container.setSlot(i, items.get(i));
            }
        }
    }

    private void confirmItemForWindowItemsContainer(Container container, int length, boolean skipInventory) {
        for (int i = 0; i < length; i++) {
            if (i >= container.getSlotCount()) {
                if (skipInventory) continue;

                inventoryContainer.confirmSlot(getInventoryOffset(container.getSlotCount(), i));
            } else {
                container.confirmSlot(i);
            }
        }
    }

    public void handleWindowItems(WrapperPlayServerWindowItems wrapper) {
        int windowId = wrapper.getWindowId();
        List<ItemStack> items = wrapper.getItems();

        if (windowId == 0) {
            for (int i = 0; i < items.size(); i++) {
                inventoryContainer.checkTransaction(i, data);
            }

            data.getTransactionTracker().pre(() -> {
                for (int i = 0; i < items.size(); i++) {
                    inventoryContainer.setSlot(i, items.get(i));
                }
            });

            data.getTransactionTracker().post(() -> {
                for (int i = 0; i < items.size(); i++) {
                    inventoryContainer.confirmSlot(i);
                }
            });
        } else {
            Container lastOpen = openContainer.getValue();
            Container oldOpen = openContainer.getOldValue();
            boolean checkLast = lastOpen.getWindowId() == windowId;
            boolean checkOld = oldOpen != null && oldOpen.getWindowId() == windowId;

            // Accounts for dogshit mc code
            if (checkLast && checkOld) {
                if (items.size() > lastOpen.getSlotCount() || items.size() > oldOpen.getSlotCount()) {
                    attemptResync();
                }
            }

            if (checkLast) {
                checkTransactionsForWindowItems(lastOpen, items.size(), false);
            }

            if (checkOld) {
                checkTransactionsForWindowItems(oldOpen, items.size(), checkLast);
            }

            data.getTransactionTracker().pre(() -> {
                if (checkLast) {
                    setItemsForWindowItems(lastOpen, items, false);
                }

                if (checkOld) {
                    setItemsForWindowItems(oldOpen, items, checkLast);
                }
            });

            data.getTransactionTracker().post(() -> {
                if (checkLast) {
                    confirmItemForWindowItemsContainer(lastOpen, items.size(), false);
                }

                if(checkOld) {
                    confirmItemForWindowItemsContainer(oldOpen, items.size(), checkLast);
                }
            });
        }
    }

    public boolean handleSetSlot(WrapperPlayServerSetSlot wrapper) {
        int windowId = wrapper.getWindowId();
        int slot = wrapper.getSlot();

        // The problem is that if the player is in a creative tab that is not the inventory it will just cancel the set slot
        // Prevent inventory desyncing when player is in creative mode (cancel the set slot packet)
        if (!(windowId == 0 && slot >= 36 && slot < 45) && windowId == 0) {
            boolean canBeDesynced = data.getStateTracker().getGameMode().getValue() == GameMode.CREATIVE
                    || (data.getStateTracker().getGameMode().getOldValue() != null && data.getStateTracker().getGameMode().getOldValue() == GameMode.CREATIVE);

            if (canBeDesynced) {
                attemptResync();
                return true;
            }
        }

        if (windowId == -1) {
            cursorItemStack.checkTransaction(data);
        } else if (windowId == 0 && slot >= 36 && slot < 45) {
            inventoryContainer.checkTransaction(slot, data);
        }

        data.getTransactionTracker().pre(() -> {
            if (windowId == -1) {
                cursorItemStack.setValue(wrapper.getItem());
            } else if (windowId == 0 && slot >= 36 && slot < 45) {
                inventoryContainer.setSlot(slot, wrapper.getItem());
            } else {
                if (windowId == 0) {
                    // We have to check for the desync again because the gamemode could have been changed before
                    // The even more correct mathematically way would be to save upcoming gamemode changes and cancel beforehand
                    boolean canBeDesynced = data.getStateTracker().getGameMode().getValue() == GameMode.CREATIVE
                            || (data.getStateTracker().getGameMode().getOldValue() != null && data.getStateTracker().getGameMode().getOldValue() == GameMode.CREATIVE);

                    if (canBeDesynced) {
                        attemptResync();
                    }
                }

                Container lastOpen = openContainer.getValue();
                Container oldOpen = openContainer.getOldValue();

                boolean checkLast = lastOpen.getWindowId() == windowId;
                boolean checkOld = oldOpen != null && oldOpen.getWindowId() == windowId;

                if (checkLast) {
                    if (slot >= lastOpen.getSlotCount()) {
                        inventoryContainer.setSlot(getInventoryOffset(lastOpen.getSlotCount(), slot), wrapper.getItem());
                    } else {
                        lastOpen.setSlot(slot, wrapper.getItem());
                    }
                }

                if (checkOld) {
                    if (slot >= oldOpen.getSlotCount()) {
                        if (!checkLast) {
                            inventoryContainer.setSlot(getInventoryOffset(oldOpen.getSlotCount(), slot), wrapper.getItem());
                        }
                        attemptResync();
                    } else {
                        oldOpen.setSlot(slot, checkLast ? wrapper.getItem().copy() : wrapper.getItem());
                    }
                }
            }
        });


        data.getTransactionTracker().post(() -> {
            if (windowId == -1) {
                cursorItemStack.confirm();
            } else if (windowId == 0 && slot >= 36 && slot < 45) {
                inventoryContainer.confirmSlot(slot);
            } else {
                Container lastOpen = openContainer.getValue();
                Container oldOpen = openContainer.getOldValue();

                boolean checkLast = lastOpen.getWindowId() == windowId;
                boolean checkOld = oldOpen != null && oldOpen.getWindowId() == windowId;

                if (checkLast) {
                    if (slot >= lastOpen.getSlotCount()) {
                        inventoryContainer.confirmSlot(getInventoryOffset(lastOpen.getSlotCount(), slot));
                    } else {
                        lastOpen.confirmSlot(slot);
                    }
                }

                if (checkOld) {
                    if (slot >= oldOpen.getSlotCount()) {
                        if (!checkLast) {
                            inventoryContainer.confirmSlot(getInventoryOffset(oldOpen.getSlotCount(), slot));
                        }
                    } else {
                        oldOpen.confirmSlot(slot);
                    }
                }
            }
        });

        return false;
    }

    public int getInventoryOffset(int openWindowSlots, int slot) {
        return slot - openWindowSlots + 9;
    }

    // Client tracking: all values can be set immediate
    public void handleClientCloseWindow() {
        openContainer.setValueCertainly(inventoryContainer);
        cursorItemStack.setValueCertainly(ItemStack.EMPTY);
    }

    public void handleCreativeInventoryAction(WrapperPlayClientCreativeInventoryAction wrapper) {
        // We only update these inventory slots
        if (wrapper.getSlot() < 1 || wrapper.getSlot() > 44) {
            return;
        }

        boolean inCreative = data.getStateTracker().getGameMode().getValue().equals(GameMode.CREATIVE)
                || (data.getStateTracker().getGameMode().getOldValue() != null && data.getStateTracker().getGameMode().getOldValue() == GameMode.CREATIVE);

        if (inCreative) {
            inventoryContainer.getSlot(wrapper.getSlot()).setValueCertainly(wrapper.getItemStack());
        } else {
            // TODO: BAN????
            data.getUser().closeConnection();
        }
    }

    public void handleClientHeldItemChange(WrapperPlayClientHeldItemChange wrapper) {
        // No need to listen to server packet as the client should always update us
        heldSlot = Math.max(Math.min(wrapper.getSlot(), 8), 0); // Clamp between 0-8
    }

    public void handleClientClickWindow(WrapperPlayClientClickWindow wrapper) {
        int containerId = wrapper.getWindowId();
        int slot = wrapper.getSlot();
        int button = wrapper.getButton();
        ItemStack carriedItem = wrapper.getCarriedItemStack();
        WindowClickType clickType = wrapper.getWindowClickType();

        Container lastOpen = openContainer.getValue();

        if (containerId == 0) {
            processClickWindow(inventoryContainer, lastOpen, slot, clickType, button, carriedItem);
        } else {
            Container oldOpen = openContainer.getOldValue();

            if (lastOpen.getWindowId() == containerId) {
                if (slot >= lastOpen.getSlotCount()) {
                    processClickWindow(inventoryContainer, lastOpen, getInventoryOffset(lastOpen.getSlotCount(), slot), clickType, button, carriedItem);
                } else {
                    processClickWindow(lastOpen, lastOpen, slot, clickType, button, carriedItem);
                }
            } else if (oldOpen != null && oldOpen.getWindowId() == containerId) {
                if (slot >= oldOpen.getSlotCount()) {
                    processClickWindow(inventoryContainer, oldOpen, getInventoryOffset(oldOpen.getSlotCount(), slot), clickType, button, carriedItem);
                } else {
                    processClickWindow(oldOpen, oldOpen, slot, clickType, button, carriedItem);
                }
            }
        }
    }

    public void processClickWindow(Container clickedContainer, Container openContainer, int slot, WindowClickType clickType, int button, ItemStack carriedItem) {
        switch (clickType) {
            // Both left and right mouse click
            case PICKUP:
                processPickup(clickedContainer, slot, carriedItem, button == 0);
                break;
            // Left/right shift click an item (identical implementation)
            case QUICK_MOVE:
                processShiftClick(clickedContainer, openContainer, slot, carriedItem);
                break;
            case SWAP:
                processNumberMove(clickedContainer, slot, button);
                break;
            // No need to implement middle click
            case QUICK_CRAFT:
                processDrag(clickedContainer, openContainer, slot, button, carriedItem);
                break;
            case PICKUP_ALL:
                processPickupAll(openContainer);
                break;
        }
    }

    public void processPickup(Container clickedContainer, int slot, ItemStack clickedItem, boolean leftClick) {
        ItemStack cursorItem = cursorItemStack.getValue();

        // Slot -1 isn't a real slot it's just the menu
        if (slot == -1) {
            return;
        }

        // The player dropped all the items
        if (slot == -999) {
            if (leftClick) {
                cursorItemStack.setValueCertainly(ItemStack.EMPTY);
            } else {
                cursorItemStack.getValue().setAmount(cursorItemStack.getValue().getAmount() - 1);

                if (cursorItemStack.getValue().getAmount() == 0) {
                    cursorItemStack.setValueCertainly(ItemStack.EMPTY);
                }
            }

            return;
        }

        // We could do all the inventory calculations for the old item also but I'm too lazy
        if (cursorItemStack.getOldValue() != null) {
            attemptResync();
        }

        // If the cursor item is null then we just take the item out
        if (cursorItem == ItemStack.EMPTY) {
            if (leftClick || clickedItem == ItemStack.EMPTY) {
                // Left click takes everything
                cursorItemStack.setValueCertainly(clickedItem);
                clickedContainer.getSlot(slot).setValueCertainly(ItemStack.EMPTY);
            } else {
                // Right click takes half
                int taken = (clickedItem.getAmount() + 1) / 2;
                ItemStack result = clickedItem.copy();
                result.setAmount(taken);
                cursorItemStack.setValueCertainly(result);

                clickedItem.setAmount(clickedItem.getAmount() - taken);
                if (clickedItem.getAmount() != 0) {
                    clickedContainer.getSlot(slot).setValueCertainly(clickedItem);
                } else {
                    clickedContainer.getSlot(slot).setValueCertainly(ItemStack.EMPTY);
                }
            }

            return;
        }

        // Hack to add crafting result to cursor
        if ((clickedContainer.getWindowType() == WindowType.INVENTORY || clickedContainer.getWindowType() == WindowType.CRAFTING_TABLE) && slot == 0) {
            if (ItemUtils.isEquivalent(cursorItem, clickedItem) && (cursorItem.getAmount() + clickedItem.getAmount()) <= cursorItem.getType().getMaxAmount()) {
                cursorItem.setAmount(cursorItem.getAmount() + clickedItem.getAmount());
            }

            return;
        }

        // Check if the item can be added in the slot
        if (!ContainerUtils.isValidItemForSlot(clickedContainer, slot, cursorItem)) {
            return;
        }

        // A hack to act like we are adding one bucket to the furnace
        if (clickedContainer.getWindowType() == WindowType.FURNACE && slot == 1 && cursorItem.getType() == ItemTypes.BUCKET) {
            leftClick = false;
        }

        // A hack to act like we adding one item to beacon / enchanting table
        // Enchanting tables: anything can be put in it, but it will only put one in of it
        // Beacons: any INGOT can be put in it, but it will only put one in of it, we do the ingot check above
        if ((clickedContainer.getWindowType() == WindowType.ENCHANTING_TABLE
                || clickedContainer.getWindowType() == WindowType.BEACON) && slot == 0) {
            leftClick = false;
        }

        // Brewing stand can only put in one glass bottle
        if (clickedContainer.getWindowType() == WindowType.BREWING_STAND && slot >= 0 && slot <= 2) {
            leftClick = false;
        }

        // Check if we are working with the armor slots
        if (clickedContainer.getWindowType() == WindowType.INVENTORY && slot >= 5 && slot <= 8) {
            // Check if armor can be applied
            if (slot != ContainerUtils.getArmorSlotForItem(cursorItem.getType(), true)) {
                return;
            }
        }

        if (clickedContainer.getWindowType() == WindowType.VILLAGER && slot == 2) {
            if (ItemUtils.canAddItem(cursorItem, clickedItem)) {
                int computedCount = Math.min(cursorItem.getAmount() + clickedItem.getAmount(), cursorItem.getType().getMaxAmount());
                int toAdd = computedCount - cursorItem.getAmount();

                cursorItem.setAmount(cursorItem.getAmount() + toAdd);
                clickedContainer.getSlot(slot).setValueCertainly(cursorItem);
            }

            return;
        }

        // It is an empty slot
        if (clickedItem == ItemStack.EMPTY) {
            if (leftClick) {
                // Set slot to cursor and cursor to null
                clickedContainer.getSlot(slot).setValueCertainly(cursorItem);
                cursorItemStack.setValueCertainly(ItemStack.EMPTY);
            } else {
                if (cursorItem.getAmount() > 0) {
                    ItemStack result = cursorItem.copy();
                    result.setAmount(1);

                    clickedContainer.getSlot(slot).setValueCertainly(result);
                    cursorItem.setAmount(cursorItem.getAmount() - 1);
                    if (cursorItem.getAmount() == 0) {
                        cursorItemStack.setValueCertainly(ItemStack.EMPTY);
                    }

                    return;
                }
            }

            return;
        }

        if (ItemUtils.canAddItem(clickedItem, cursorItem)) {
            int computedCount = Math.min(clickedItem.getAmount() + (leftClick ? cursorItem.getAmount() : 1), clickedItem.getType().getMaxAmount());
            int toAdd = computedCount - clickedItem.getAmount();

            clickedItem.setAmount(clickedItem.getAmount() + toAdd);
            clickedContainer.getSlot(slot).setValueCertainly(clickedItem);
            cursorItem.setAmount(cursorItem.getAmount() - toAdd);

            if (cursorItem.getAmount() == 0) {
                cursorItemStack.setValueCertainly(ItemStack.EMPTY);
            }

            return;
        }

        // Can only switch inside an enchanting table / beacon if the count of the item is 1
        if ((clickedContainer.getWindowType() == WindowType.ENCHANTING_TABLE || clickedContainer.getWindowType() == WindowType.BEACON) && cursorItem.getAmount() > 1) {
            return;
        }

        // Can't switch if same
        if (ItemUtils.isItemEqual(cursorItem, clickedItem)) {
            return;
        }

        clickedContainer.getSlot(slot).setValueCertainly(cursorItem);
        cursorItemStack.setValueCertainly(clickedItem);
    }

    public void processShiftClick(Container clickedContainer, Container openContainer, int slot, ItemStack clickedItem) {
        // Thank you minecraft. If the item is unable to move the client will set clicked item to null
        // This means we don't have to implement shift click slot checks like in anvils
        if (clickedItem == ItemStack.EMPTY) {
            return;
        }

        // You can't shift click negative / 0 items
        if (clickedItem.getAmount() <= 0) {
            return;
        }

        boolean nullify = false;

        // Shift move inside the inventory
        if (openContainer == inventoryContainer) {
            int armorIndex = ContainerUtils.getArmorSlotForItem(clickedItem.getType(), false); // 0 helmet -> 3 boots
            if (slot >= 9 && slot <= 44 && armorIndex != Integer.MIN_VALUE) {
                ConfirmableState<ItemStack> armorState = clickedContainer.getSlot(armorIndex);

                // Old  &  New
                // AIR  -  ITEM  -> Uncertain
                // ITEM -  AIR   -> Uncertain
                // AIR  -  AIR   -> Fine
                // ITEM -  ITEM  -> Fine
                if (armorState.getOldValue() == ItemStack.EMPTY && armorState.getValue() != ItemStack.EMPTY) {
                    attemptResync();
                }

                if (armorState.getOldValue() != null && armorState.getValue() == ItemStack.EMPTY) {
                    attemptResync();
                }

                if (armorState.getValue() == ItemStack.EMPTY) {
                    armorState.setValueCertainly(clickedItem);
                    clickedContainer.getSlot(slot).setValueCertainly(ItemStack.EMPTY);
                    return;
                }
            }

            if (slot >= 9 && slot <= 35) {
                nullify = doTransfer(clickedItem, inventoryContainer, 36, 44, false);
            } else if (slot <= 8) {
                if (slot == 0) { // We don't account for recipes
                    attemptResync();
                }

                nullify = doTransfer(clickedItem, inventoryContainer, 9, 44, slot == 0);
            } else if (slot <= 44) {
                nullify = doTransfer(clickedItem, inventoryContainer, 9, 35, false);
            }
        }  else if (clickedContainer == openContainer) {
            boolean notReversed =
                    (clickedContainer.getWindowType() == WindowType.ANVIL && slot != 2) // Anvis are not reversed except slot 2
                            || (clickedContainer.getWindowType() == WindowType.CRAFTING_TABLE && slot != 0) // Crafting window is not reversed except slot 0
                            || clickedContainer.getWindowType() == WindowType.FURNACE // Furnace is not reversed
                            || (clickedContainer.getWindowType() == WindowType.VILLAGER && slot != 2); // Villagers are not reversed except slot 2

            // We don't account for trades
            if (clickedContainer.getWindowType() == WindowType.VILLAGER && slot == 2) {
                attemptResync();
            }

            // We don't account for crafting
            if (clickedContainer.getWindowType() == WindowType.CRAFTING_TABLE && slot == 0) {
                attemptResync();
            }

            nullify = doTransfer(clickedItem, inventoryContainer, 9, 44, !notReversed);
        } else {
            // Some stuff will can move inside the inventory even if the target slots of the open container are full
            boolean canMove = true;

            if (openContainer.getWindowType() == WindowType.BREWING_STAND && ItemUtils.isBrewingMaterial(clickedItem.getType())) {
                nullify = doTransfer(clickedItem, openContainer, 3, 3, false, false, true, false);
            } else if (openContainer.getWindowType() == WindowType.BREWING_STAND && ItemUtils.isBottle(clickedItem.getType())) {
                nullify = doTransfer(clickedItem, openContainer, 0, 2, false);
                canMove = false;
            } else if (openContainer.getWindowType() == WindowType.FURNACE && ItemUtils.isFurnaceFuel(clickedItem.getType(), false)) {
                nullify = doTransfer(clickedItem, openContainer, 1, 1, false);
                canMove = false;
            } else if (false/* && ItemChecks.hasFurnaceRecipe(clickedItem) */) { // TODO: make furnace recipes hahah :(((((((((((((
                nullify = doTransfer(clickedItem, openContainer, 0, 0, false);
                canMove = false;
            } else if (openContainer.getWindowType() == WindowType.ENCHANTING_TABLE && ItemUtils.isLapis(clickedItem)) {
                nullify = doTransfer(clickedItem, openContainer, 1, 1, false);
                canMove = false;
            } else if (openContainer.getWindowType() == WindowType.ENCHANTING_TABLE && !ItemUtils.isLapis(clickedItem)) {
                nullify = doTransfer(clickedItem, openContainer, 0, 0, false, false, true, true);
                canMove = false;
            } else if (openContainer.getWindowType() == WindowType.BEACON && ItemUtils.isBeaconIngot(clickedItem.getType()) && clickedItem.getAmount() == 1) {
                nullify = doTransfer(clickedItem, openContainer, 0, 0, false, false, true, false); // put only one not needed here since clicked item is one anyways
                canMove = false;
            } else if (openContainer.getWindowType() == WindowType.HORSE && clickedItem.getType() == ItemTypes.SADDLE) {
                nullify = doTransfer(clickedItem, openContainer, 0, 0, false);
                canMove = false;
            } else if (openContainer.getWindowType() == WindowType.HORSE && ItemUtils.isHorseArmor(clickedItem.getType())) {
                nullify = doTransfer(clickedItem, openContainer, 1, 1, false);
                canMove = false;
            }  else if (openContainer.getWindowType() == WindowType.ANVIL) {
                nullify = doTransfer(clickedItem, openContainer, 0, 1, false, true, true, false);
                canMove = false;
            } else if (openContainer.getWindowType() == WindowType.NORMAL) {
                nullify = doTransfer(clickedItem, openContainer, 0, openContainer.getSlotCount() - 1, false);
                canMove = false;
            }

            if (!nullify && canMove) {
                if (slot >= 9 && slot <= 35) {
                    nullify = doTransfer(clickedItem, inventoryContainer, 36, 44, false);
                } else if (slot >= 36 && slot <= 44) {
                    nullify = doTransfer(clickedItem, inventoryContainer, 9, 35, false);
                }
            }
        }

        if (nullify) {
            clickedContainer.getSlot(slot).setValueCertainly(ItemStack.EMPTY);
        } else {
            clickedContainer.getSlot(slot).setValueCertainly(clickedItem);
        }
    }

    public void processNumberMove(Container clickedContainer, int slot, int button) {
        int inventorySlot = 36 + button;

        ConfirmableState<ItemStack> clickedState = clickedContainer.getSlot(slot);
        ConfirmableState<ItemStack> inventoryState = inventoryContainer.getSlot(inventorySlot);
        ItemStack clickedItem = clickedState.getValue();
        ItemStack inventoryItem = inventoryState.getValue();


        if (clickedState.getOldValue() != null || inventoryState.getOldValue() != null) {
            attemptResync();
        }

        // Try to move it in
        if (clickedItem == ItemStack.EMPTY) {
            if (!ContainerUtils.isValidItemForSlot(clickedContainer, slot, inventoryItem)) {
                return;
            }

            clickedState.setValueCertainly(inventoryItem);
            inventoryState.setValueCertainly(ItemStack.EMPTY);
            return;
        }

        if (inventoryItem == ItemStack.EMPTY) {
            inventoryState.setValueCertainly(clickedItem);
            clickedState.setValueCertainly(ItemStack.EMPTY);
            return;
        }

        int emptySlot = -1;

        // Minecraft first checks hotbar for some reason
        for (int i = 36; i <= 44; i++) {
            ConfirmableState<ItemStack> state = inventoryContainer.getSlot(i);

            if (state.getValue() == ItemStack.EMPTY) {
                emptySlot = i;

                if (state.getOldValue() != null && state.getOldValue() != ItemStack.EMPTY) {
                    attemptResync();
                }
                break;
            }
        }

        if (emptySlot == -1) {
            for (int i = 9; i <= 35; i++) {
                ConfirmableState<ItemStack> state = inventoryContainer.getSlot(i);

                if (state.getValue() == ItemStack.EMPTY) {
                    emptySlot = i;

                    if (state.getOldValue() != null && state.getOldValue() != ItemStack.EMPTY) {
                        attemptResync();
                    }
                    break;
                }
            }
        }

        // If there are no empty slots its impossible to move
        if (emptySlot == -1) {
            return;
        }

        // This should be the transfering logic if both slots have an item in them
        clickedState.setValueCertainly(ItemStack.EMPTY);
        inventoryState.setValueCertainly(clickedItem);

        if (clickedContainer.getWindowType() == WindowType.INVENTORY && ContainerUtils.isValidItemForSlot(clickedContainer, slot, inventoryItem)) {
            clickedState.setValueCertainly(inventoryItem);
            return;
        }

        boolean nullify = doTransfer(inventoryItem, inventoryContainer, 36, 44, false, true, false, false);

        if (!nullify) {
            nullify = doTransfer(inventoryItem, inventoryContainer, 9, 35, false, true, false, false);
        }

        if (!nullify) {
            inventoryContainer.getSlot(emptySlot).setValueCertainly(inventoryItem);
        }
    }

    public void processDrag(Container clickedContainer, Container openContainer, int slot, int button, ItemStack itemStack) {
        int dragAction = (button & 3);
        int dragLimit = (button >> 2) & 3;

        if (slot == -999) {
            // Start of drag
            if (dragAction == 0) {
                switch (dragLimit) {
                    case 0:
                        dragMode = DragType.SPLIT;
                        break;
                    case 1:
                        dragMode = DragType.ONE;
                        break;
                    case 3:
                        break;
                }
            } else if (dragAction == 2) {
                finishDrag(openContainer);
            }
        } else {
            dragSlots.add(new DragSlot(slot, clickedContainer == inventoryContainer));
        }
    }

    public void finishDrag(Container openContainer) {
        ItemStack stack = cursorItemStack.getValue();

        int size = dragMode == DragType.SPLIT ? MCMath.floor_float((float)stack.getAmount() / (float)dragSlots.size()) : 1;

        for (DragSlot dragSlot : dragSlots) {
            int slot = dragSlot.getSlot();
            Container container = dragSlot.isInventory() ? inventoryContainer : openContainer;

            if (ContainerUtils.isValidItemForSlot(container, slot, stack)) {
                // TODO: support split states here,
                ConfirmableState<ItemStack> state = container.getSlot(slot);
                ItemStack last = state.getValue();

                int added = 0;
                if (last == ItemStack.EMPTY) {
                    ItemStack copy = stack.copy();
                    added = Math.min(size, stack.getAmount());
                    copy.setAmount(added);
                    state.setValueCertainly(copy);
                } else if (ItemUtils.canAddItem(last, stack)) {
                    int previous = last.getAmount();
                    last.setAmount(Math.min(last.getMaxStackSize(), last.getAmount() + Math.min(size, stack.getAmount())));
                    added = last.getAmount() - previous;
                }

                stack.setAmount(stack.getAmount() - added);
            }
        }

        if (stack.isEmpty()) {
            cursorItemStack.setValueCertainly(ItemStack.EMPTY);
        }

        dragSlots.clear();
        dragMode = DragType.NONE;

    }

    public void processPickupAll(Container openContainer) {
        ItemStack cursor = cursorItemStack.getValue();
        int left = cursor.getType().getMaxAmount() - cursor.getAmount();
        int i = 0;

        Container loopedContainer = openContainer;
        while (left > 0 && i < loopedContainer.getSlotCount()) {
            ItemStack slotItem = loopedContainer.getSlot(i).getValue();

            if (slotItem.getAmount() != slotItem.getType().getMaxAmount() && ItemUtils.canAddItem(cursor, slotItem)) {
                int toRemove = Math.min(left, slotItem.getAmount());
                slotItem.setAmount(slotItem.getAmount() - toRemove);
                left -= toRemove;
                cursor.setAmount(cursor.getAmount() + toRemove);

                if (slotItem.getAmount() <= 0) {
                    loopedContainer.getSlot(i).setValueCertainly(ItemStack.EMPTY);
                }
            }

            i++;

            // Switch container of loop to inventory
            if (i == loopedContainer.getSlotCount()) {
                if (loopedContainer != inventoryContainer) {
                    loopedContainer = inventoryContainer;
                    i = 0;
                }
            }
        }
    }

    private boolean doTransfer(ItemStack clickedItem, Container targetContainer, int toInvMin, int toInvMax, boolean reversed) {
        return doTransfer(clickedItem, targetContainer, toInvMin, toInvMax, reversed, true, true, false);
    }

    private boolean doTransfer(ItemStack clickedItem, Container targetContainer, int toInvMin, int toInvMax, boolean reversed, boolean canAdd, boolean addToEmpty, boolean putOnlyOne) {
        if (canAdd) {
            for (int i = toInvMin; i <= toInvMax; i++) {
                // Reverses the slot if requested
                int slot = reversed ? (toInvMax - (i - toInvMin)) : i;

                ConfirmableState<ItemStack> loopedState = targetContainer.getSlot(slot);
                ItemStack latestSlot = loopedState.getValue();

                int oldSlotAdded = 0;

                // Do the same calculation for the old slot, if we can move more or less items to the old slot then we are uncertain about the inventory
                if (loopedState.getOldValue() != null) {
                    ItemStack oldSlot = loopedState.getOldValue();

                    if (oldSlot.getAmount() < oldSlot.getType().getMaxAmount() && ItemUtils.isItemEqual(oldSlot, clickedItem)) {
                        oldSlotAdded = Math.min(oldSlot.getType().getMaxAmount() - oldSlot.getAmount(), clickedItem.getAmount());
                    }
                }

                if (latestSlot != null && latestSlot.getAmount() < latestSlot.getType().getMaxAmount() && ItemUtils.isEquivalent(latestSlot, clickedItem)) {
                    int toAdd = Math.min(latestSlot.getType().getMaxAmount() - latestSlot.getAmount(), clickedItem.getAmount());
                    clickedItem.setAmount(clickedItem.getAmount() - toAdd);
                    latestSlot.setAmount(latestSlot.getAmount() + toAdd);

                    // The same amount wouldn't be added to old slot
                    if (loopedState.getOldValue() != null && oldSlotAdded != toAdd) {
                        attemptResync();
                    }

                    if (clickedItem.getAmount() == 0) {
                        return true;
                    }
                } else if (oldSlotAdded > 0) {
                    // Normally the item would be added to the old slot
                    attemptResync();
                }
            }
        }

        if (addToEmpty) {
            for (int i = toInvMin; i <= toInvMax; i++) {
                // Reverses the slot if requested
                int slot = reversed ? (toInvMax - (i - toInvMin)) : i;
                ConfirmableState<ItemStack> loopedState = targetContainer.getSlot(slot);
                ItemStack latestSlot = loopedState.getValue();

                // Old  &  New
                // AIR  -  ITEM  -> Uncertain
                // ITEM -  AIR   -> Uncertain
                // AIR  -  AIR   -> Fine
                // ITEM -  ITEM  -> Fine
                if (loopedState.getOldValue() != null) {
                    ItemStack oldSlot = loopedState.getOldValue();
                    if (oldSlot == ItemStack.EMPTY && latestSlot != ItemStack.EMPTY ||
                            (oldSlot != ItemStack.EMPTY && latestSlot == ItemStack.EMPTY)) {
                        attemptResync();
                    }
                }

                if (latestSlot == ItemStack.EMPTY) {
                    if (putOnlyOne) {
                        ItemStack copied = clickedItem.copy();
                        copied.setAmount(1);
                        loopedState.setValueCertainly(copied);
                        clickedItem.setAmount(clickedItem.getAmount() - 1);

                        if (clickedItem.getAmount() == 0) {
                            return true;
                        }
                    } else {
                        loopedState.setValueCertainly(clickedItem);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void handleBlockPlace(WrapperPlayClientPlayerBlockPlacement wrapper) {
        if (wrapper.getFace() != BlockFace.OTHER) {
            return;
        }

        if (!wrapper.getItemStack().isPresent()) {
            return;
        }

        ItemStack itemStack = wrapper.getItemStack().get();
        int targetSlot = ContainerUtils.getArmorSlotForItem(itemStack.getType(), false);

        if (targetSlot != Integer.MIN_VALUE) {
            ConfirmableState<ItemStack> slot = inventoryContainer.getSlot(targetSlot);
            if (slot.getValue() == ItemStack.EMPTY) {
                slot.setValueCertainly(itemStack);
            }
        }
    }

    // Debug purposes
    public void sendInventory(boolean dumb) {
        data.getUser().sendPacket(new WrapperPlayServerOpenWindow(127, "minecraft:container", Component.text("debug vieww"), 45, 0));

        List<ItemStack> stack = new ArrayList<>();

        for (ConfirmableState<ItemStack> lol : inventoryContainer.getContentsForDebug()) {
            if (lol.getValue() == null) {
                stack.add(ItemStack.EMPTY);
                continue;
            }

            ItemStack itemStack = ItemStack.builder()
                    .amount(dumb ? -5 : lol.getValue().getAmount())
                    .type(lol.getValue().getType())
                    .nbt(lol.getValue().getNBT())
                    .legacyData(lol.getValue().getLegacyData())
                    .build();

            stack.add(itemStack);
        }

        data.getUser().sendPacket(new WrapperPlayServerWindowItems(127, 0, stack, null));
        data.getUser().sendPacket(new WrapperPlayServerSetSlot(127, 0, 45 + 9 * 4 - 1, ItemStack.builder().amount(3 + ThreadLocalRandom.current().nextInt(2)).type(ItemTypes.IRON_BLOCK).build()));
    }
}
