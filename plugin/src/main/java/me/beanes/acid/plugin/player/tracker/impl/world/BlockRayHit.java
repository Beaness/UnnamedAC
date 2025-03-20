package me.beanes.acid.plugin.player.tracker.impl.world;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.util.Vector3d;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BlockRayHit {
    private int x, y, z;
    private Vector3d interceptPoint;
    private BlockFace plane;
}
