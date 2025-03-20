package me.beanes.acid.plugin.player.tracker.impl.inventory;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DragSlot {
    private int slot;
    private boolean inventory;

    @Override
    public int hashCode() {
        return ((inventory ? 1 : 0) << 31) | (slot & 0x7FFFFFFF);
    }
}
