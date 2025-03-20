package me.beanes.acid.plugin.player.tracker.impl.world.resync;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QueuedResync {
    private final short compactedBlockPos;
    private final int blockId;
}
