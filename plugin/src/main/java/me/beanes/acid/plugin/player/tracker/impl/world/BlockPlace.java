package me.beanes.acid.plugin.player.tracker.impl.world;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.util.Vector3i;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BlockPlace {
    private Vector3i blockPosition;
    private BlockFace face;
    private Vector3f cursorPosition;
    private ItemStack itemStack;
}
